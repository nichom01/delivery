package com.deliverysystem.config;

import com.deliverysystem.domain.*;
import com.deliverysystem.repository.*;
import com.deliverysystem.service.PostcodeRoutingService;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class DataSeeder {
    
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    
    @Value("${application.seed.enabled:true}")
    private boolean seedEnabled;
    
    @Bean
    public CommandLineRunner seedData(
            DepotRepository depotRepository,
            RouteRepository routeRepository,
            VehicleRepository vehicleRepository,
            DriverRepository driverRepository,
            PostcodeRuleRepository postcodeRuleRepository,
            OrderRepository orderRepository,
            BoxRepository boxRepository,
            ManifestRepository manifestRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            PostcodeRoutingService postcodeRoutingService,
            EntityManager entityManager,
            PlatformTransactionManager transactionManager) {
        return args -> {
            if (!seedEnabled) {
                log.info("Data seeding is disabled");
                return;
            }
            
            // Check if all data already exists - if so, skip seeding
            long depotCount = depotRepository.count();
            long userCount = userRepository.count();
            if (depotCount >= 5 && userCount >= 5) {
                log.info("Database already contains complete seed data (depots: {}, users: {}), skipping seed", 
                    depotCount, userCount);
                return;
            }
            
            log.info("Starting data seed (existing depots: {}, existing users: {})...", depotCount, userCount);
            
            // Use TransactionTemplate for programmatic transaction management
            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
            transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            
            transactionTemplate.execute(status -> {
                try {
                    seedAllData(depotRepository, routeRepository, vehicleRepository, driverRepository, 
                        postcodeRuleRepository, orderRepository, boxRepository, manifestRepository, 
                        userRepository, passwordEncoder, postcodeRoutingService, entityManager);
                    log.info("Data seed completed successfully");
                    return null;
                } catch (Exception e) {
                    log.error("Error during data seeding", e);
                    status.setRollbackOnly();
                    throw e;
                }
            });
        };
    }
    
    private void seedAllData(
            DepotRepository depotRepository,
            RouteRepository routeRepository,
            VehicleRepository vehicleRepository,
            DriverRepository driverRepository,
            PostcodeRuleRepository postcodeRuleRepository,
            OrderRepository orderRepository,
            BoxRepository boxRepository,
            ManifestRepository manifestRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            PostcodeRoutingService postcodeRoutingService,
            EntityManager entityManager) {
        // Seed depots FIRST (users have foreign key dependency on depots)
        seedDepotsAndRoutes(depotRepository, routeRepository);
        // Flush to ensure depots are persisted before creating users
        entityManager.flush();
        // Then seed users (they reference depots)
        seedUsers(userRepository, passwordEncoder, depotRepository);
        seedVehicles(depotRepository, vehicleRepository);
        seedDrivers(depotRepository, driverRepository);
        seedPostcodeRules(routeRepository, postcodeRuleRepository, postcodeRoutingService);
        seedOrdersAndBoxes(routeRepository, orderRepository, boxRepository);
        seedManifests(routeRepository, vehicleRepository, driverRepository, boxRepository, manifestRepository);
    }
    
    private void seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder, DepotRepository depotRepository) {
        String defaultPassword = passwordEncoder.encode("password");
        LocalDateTime now = LocalDateTime.now();
        
        // Central admin
        if (!userRepository.findByUsername("admin").isPresent()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(defaultPassword);
            admin.setEmail("admin@deliverysystem.com");
            admin.setName("Chris Admin");
            admin.setRole(User.UserRole.CENTRAL_ADMIN);
            admin.setDepotId(null);
            admin.setStatus("ACTIVE");
            admin.setLastLogin(now);
            userRepository.save(admin);
            log.info("Created admin user");
        }
        
        // Depot managers - find depots by name to get their actual IDs
        String[] depotNames = {"London Central", "Manchester North", "Birmingham West", "Leeds Central"};
        String[] managerNames = {"John Smith", "Sarah Jones", "Michael Brown", "Emma Wilson"};
        String[] managerEmails = {"depot1@deliverysystem.com", "depot2@deliverysystem.com", "depot3@deliverysystem.com", "depot4@deliverysystem.com"};
        
        for (int i = 0; i < depotNames.length; i++) {
            String username = "depot" + (i + 1);
            if (!userRepository.findByUsername(username).isPresent()) {
                // Find depot by name to get its actual ID
                Depot depot = depotRepository.findByName(depotNames[i]).orElse(null);
                if (depot == null) {
                    log.warn("Depot '{}' not found, skipping user creation for {}", depotNames[i], username);
                    continue;
                }
                
                User manager = new User();
                manager.setUsername(username);
                manager.setPassword(defaultPassword);
                manager.setEmail(managerEmails[i]);
                manager.setName(managerNames[i]);
                manager.setRole(User.UserRole.DEPOT_MANAGER);
                manager.setDepotId(depot.getId()); // Use actual depot ID
                manager.setStatus("ACTIVE");
                manager.setLastLogin(now);
                userRepository.save(manager);
                log.info("Created depot manager user: {} for depot: {} (ID: {})", username, depotNames[i], depot.getId());
            }
        }
        
        log.info("Total users in database: {}", userRepository.count());
    }
    
    private void seedDepotsAndRoutes(DepotRepository depotRepository, RouteRepository routeRepository) {
        LocalDate now = LocalDate.now();
        
        // Depot 1: London Central
        Depot depot1 = depotRepository.findById("depot-1")
            .orElse(depotRepository.findByName("London Central").orElse(null));
        if (depot1 == null) {
            depot1 = new Depot();
            depot1.setId("depot-1");
            depot1.setName("London Central");
            depot1.setAddress("Vauxhall, London");
            depot1.setLatitude(new BigDecimal("51.4865"));
            depot1.setLongitude(new BigDecimal("-0.1234"));
            depotRepository.save(depot1);
            log.info("Created depot: London Central");
        }
        
        // Routes for London
        String[][] londonRoutes = {
            {"route-a", "ROUTE-A", "Route A — South West", "SW1–SW20"},
            {"route-b", "ROUTE-B", "Route B — South East", "SE1–SE25"},
            {"route-c", "ROUTE-C", "Route C — City", "EC1–EC4"},
            {"route-d", "ROUTE-D", "Route D — West End", "W1–W14, WC1–WC2"},
            {"route-e", "ROUTE-E", "Route E — North", "N1–N22"},
            {"route-f", "ROUTE-F", "Route F — East", "E1–E18"}
        };
        
        for (String[] routeData : londonRoutes) {
            if (!routeRepository.findById(routeData[0]).isPresent()) {
                Route route = new Route();
                route.setId(routeData[0]);
                route.setCode(routeData[1]);
                route.setName(routeData[2]);
                route.setDescription(routeData[3]);
                route.setDepot(depot1);
                routeRepository.save(route);
            }
        }
        
        // Depot 2: Manchester North
        Depot depot2 = depotRepository.findById("depot-2")
            .orElse(depotRepository.findByName("Manchester North").orElse(null));
        if (depot2 == null) {
            depot2 = new Depot();
            depot2.setId("depot-2");
            depot2.setName("Manchester North");
            depot2.setAddress("Salford, Manchester");
            depot2.setLatitude(new BigDecimal("53.4808"));
            depot2.setLongitude(new BigDecimal("-2.2426"));
            depotRepository.save(depot2);
            log.info("Created depot: Manchester North");
        }
        
        String[][] manchesterRoutes = {
            {"route-m1", "ROUTE-M1", "Route M1 — Central", "M1–M5"},
            {"route-m2", "ROUTE-M2", "Route M2 — North", "M6–M10"},
            {"route-m3", "ROUTE-M3", "Route M3 — South", "M11–M15"},
            {"route-m4", "ROUTE-M4", "Route M4 — East", "M16–M20"}
        };
        
        for (String[] routeData : manchesterRoutes) {
            if (!routeRepository.findById(routeData[0]).isPresent()) {
                Route route = new Route();
                route.setId(routeData[0]);
                route.setCode(routeData[1]);
                route.setName(routeData[2]);
                route.setDescription(routeData[3]);
                route.setDepot(depot2);
                routeRepository.save(route);
            }
        }
        
        // Depot 3: Birmingham West
        Depot depot3 = depotRepository.findById("depot-3")
            .orElse(depotRepository.findByName("Birmingham West").orElse(null));
        if (depot3 == null) {
            depot3 = new Depot();
            depot3.setId("depot-3");
            depot3.setName("Birmingham West");
            depot3.setAddress("Handsworth, Birmingham");
            depot3.setLatitude(new BigDecimal("52.4862"));
            depot3.setLongitude(new BigDecimal("-1.8904"));
            depotRepository.save(depot3);
            log.info("Created depot: Birmingham West");
        }
        
        String[][] birminghamRoutes = {
            {"route-b1", "ROUTE-B1", "Route B1 — Central", "B1–B5"},
            {"route-b2", "ROUTE-B2", "Route B2 — North", "B6–B10"},
            {"route-b3", "ROUTE-B3", "Route B3 — South", "B11–B15"},
            {"route-b4", "ROUTE-B4", "Route B4 — East", "B16–B20"},
            {"route-b5", "ROUTE-B5", "Route B5 — West", "B21–B25"}
        };
        
        for (String[] routeData : birminghamRoutes) {
            if (!routeRepository.findById(routeData[0]).isPresent()) {
                Route route = new Route();
                route.setId(routeData[0]);
                route.setCode(routeData[1]);
                route.setName(routeData[2]);
                route.setDescription(routeData[3]);
                route.setDepot(depot3);
                routeRepository.save(route);
            }
        }
        
        // Depot 4: Leeds Central
        Depot depot4 = depotRepository.findById("depot-4")
            .orElse(depotRepository.findByName("Leeds Central").orElse(null));
        if (depot4 == null) {
            depot4 = new Depot();
            depot4.setId("depot-4");
            depot4.setName("Leeds Central");
            depot4.setAddress("Holbeck, Leeds");
            depot4.setLatitude(new BigDecimal("53.8008"));
            depot4.setLongitude(new BigDecimal("-1.5491"));
            depotRepository.save(depot4);
            log.info("Created depot: Leeds Central");
        }
        
        String[][] leedsRoutes = {
            {"route-l1", "ROUTE-L1", "Route L1 — Central", "LS1–LS5"},
            {"route-l2", "ROUTE-L2", "Route L2 — North", "LS6–LS10"},
            {"route-l3", "ROUTE-L3", "Route L3 — South", "LS11–LS15"}
        };
        
        for (String[] routeData : leedsRoutes) {
            if (!routeRepository.findById(routeData[0]).isPresent()) {
                Route route = new Route();
                route.setId(routeData[0]);
                route.setCode(routeData[1]);
                route.setName(routeData[2]);
                route.setDescription(routeData[3]);
                route.setDepot(depot4);
                routeRepository.save(route);
            }
        }
        
        // Depot 5: Bristol South
        Depot depot5 = depotRepository.findById("depot-5")
            .orElse(depotRepository.findByName("Bristol South").orElse(null));
        if (depot5 == null) {
            depot5 = new Depot();
            depot5.setId("depot-5");
            depot5.setName("Bristol South");
            depot5.setAddress("Bedminster, Bristol");
            depot5.setLatitude(new BigDecimal("51.4545"));
            depot5.setLongitude(new BigDecimal("-2.5879"));
            depotRepository.save(depot5);
            log.info("Created depot: Bristol South");
        }
        
        String[][] bristolRoutes = {
            {"route-br1", "ROUTE-BR1", "Route BR1 — Central", "BS1–BS5"},
            {"route-br2", "ROUTE-BR2", "Route BR2 — South", "BS6–BS10"},
            {"route-br3", "ROUTE-BR3", "Route BR3 — North", "BS11–BS15"}
        };
        
        for (String[] routeData : bristolRoutes) {
            if (!routeRepository.findById(routeData[0]).isPresent()) {
                Route route = new Route();
                route.setId(routeData[0]);
                route.setCode(routeData[1]);
                route.setName(routeData[2]);
                route.setDescription(routeData[3]);
                route.setDepot(depot5);
                routeRepository.save(route);
            }
        }
        
        log.info("Seeded {} depots and {} routes", depotRepository.count(), routeRepository.count());
    }
    
    private void seedVehicles(DepotRepository depotRepository, VehicleRepository vehicleRepository) {
        List<Depot> depots = depotRepository.findAll();
        LocalDate now = LocalDate.now();
        
        String[][] vehicleData = {
            // London (8 vehicles)
            {"LK71 ABC", "Ford", "Transit", "3.5t / 12m³"},
            {"MN20 XYZ", "Mercedes", "Sprinter", "3.5t / 14m³"},
            {"PQ19 DEF", "Vauxhall", "Movano", "3.5t / 13m³"},
            {"RS22 GHI", "Ford", "Transit", "3.5t / 12m³"},
            {"TU18 JKL", "Mercedes", "Sprinter", "3.5t / 14m³"},
            {"VW21 MNO", "Vauxhall", "Movano", "3.5t / 13m³"},
            {"XY23 PQR", "Ford", "Transit", "3.5t / 12m³"},
            {"AB24 STU", "Mercedes", "Sprinter", "3.5t / 14m³"},
            // Manchester (5 vehicles)
            {"MC25 VWX", "Ford", "Transit", "3.5t / 12m³"},
            {"MC26 YZA", "Mercedes", "Sprinter", "3.5t / 14m³"},
            {"MC27 BCD", "Vauxhall", "Movano", "3.5t / 13m³"},
            {"MC28 EFG", "Ford", "Transit", "3.5t / 12m³"},
            {"MC29 HIJ", "Mercedes", "Sprinter", "3.5t / 14m³"},
            // Birmingham (6 vehicles)
            {"BH30 KLM", "Ford", "Transit", "3.5t / 12m³"},
            {"BH31 NOP", "Mercedes", "Sprinter", "3.5t / 14m³"},
            {"BH32 QRS", "Vauxhall", "Movano", "3.5t / 13m³"},
            {"BH33 TUV", "Ford", "Transit", "3.5t / 12m³"},
            {"BH34 WXY", "Mercedes", "Sprinter", "3.5t / 14m³"},
            {"BH35 ZAB", "Vauxhall", "Movano", "3.5t / 13m³"},
            // Leeds (4 vehicles)
            {"LS36 CDE", "Ford", "Transit", "3.5t / 12m³"},
            {"LS37 FGH", "Mercedes", "Sprinter", "3.5t / 14m³"},
            {"LS38 IJK", "Vauxhall", "Movano", "3.5t / 13m³"},
            {"LS39 LMN", "Ford", "Transit", "3.5t / 12m³"},
            // Bristol (3 vehicles)
            {"BS40 OPQ", "Ford", "Transit", "3.5t / 12m³"},
            {"BS41 RST", "Mercedes", "Sprinter", "3.5t / 14m³"},
            {"BS42 UVW", "Vauxhall", "Movano", "3.5t / 13m³"}
        };
        
        int vehicleIndex = 0;
        int[] vehiclesPerDepot = {8, 5, 6, 4, 3};
        
        for (int depotIndex = 0; depotIndex < depots.size() && depotIndex < vehiclesPerDepot.length; depotIndex++) {
            Depot depot = depots.get(depotIndex);
            for (int i = 0; i < vehiclesPerDepot[depotIndex] && vehicleIndex < vehicleData.length; i++) {
                Vehicle vehicle = new Vehicle();
                vehicle.setRegistration(vehicleData[vehicleIndex][0]);
                vehicle.setMake(vehicleData[vehicleIndex][1]);
                vehicle.setModel(vehicleData[vehicleIndex][2]);
                vehicle.setCapacity(vehicleData[vehicleIndex][3]);
                vehicle.setMotDate(now.plusMonths(6));
                vehicle.setLastServiceDate(now.minusMonths(2));
                vehicle.setNextServiceDue(now.plusMonths(4));
                vehicle.setDepot(depot);
                vehicle.setStatus("ACTIVE");
                vehicleRepository.save(vehicle);
                vehicleIndex++;
            }
        }
        
        log.info("Seeded {} vehicles", vehicleRepository.count());
    }
    
    private void seedDrivers(DepotRepository depotRepository, DriverRepository driverRepository) {
        List<Depot> depots = depotRepository.findAll();
        LocalDate now = LocalDate.now();
        
        String[][] driverData = {
            // London (12 drivers)
            {"J. Patel", "07123456789", "PATEL123456", "2027-06-15"},
            {"R. Ahmed", "07123456790", "AHMED123456", "2027-07-20"},
            {"S. Williams", "07123456791", "WILLI123456", "2027-08-10"},
            {"T. O'Brien", "07123456792", "OBRIE123456", "2027-09-05"},
            {"M. Khan", "07123456793", "KHAN1234567", "2027-10-12"},
            {"C. Davis", "07123456794", "DAVIS123456", "2027-11-18"},
            {"A. Singh", "07123456795", "SINGH123456", "2027-12-22"},
            {"L. Brown", "07123456796", "BROWN123456", "2028-01-08"},
            {"K. Wilson", "07123456797", "WILSO123456", "2028-02-14"},
            {"D. Taylor", "07123456798", "TAYLO123456", "2028-03-20"},
            {"P. Jones", "07123456799", "JONES123456", "2028-04-25"},
            {"N. Miller", "07123456800", "MILLE123456", "2028-05-30"},
            // Manchester (8 drivers)
            {"M. Johnson", "07123456801", "JOHNS123456", "2027-07-15"},
            {"J. White", "07123456802", "WHITE123456", "2027-08-20"},
            {"R. Harris", "07123456803", "HARRI123456", "2027-09-10"},
            {"S. Martin", "07123456804", "MARTI123456", "2027-10-05"},
            {"T. Thompson", "07123456805", "THOMP123456", "2027-11-12"},
            {"A. Garcia", "07123456806", "GARCI123456", "2027-12-18"},
            {"L. Martinez", "07123456807", "MARTI123457", "2028-01-22"},
            {"K. Robinson", "07123456808", "ROBIN123456", "2028-02-08"},
            // Birmingham (9 drivers)
            {"C. Clark", "07123456809", "CLARK123456", "2027-08-15"},
            {"D. Rodriguez", "07123456810", "RODRI123456", "2027-09-20"},
            {"E. Lewis", "07123456811", "LEWIS123456", "2027-10-10"},
            {"F. Lee", "07123456812", "LEE12345678", "2027-11-05"},
            {"G. Walker", "07123456813", "WALKE123456", "2027-12-12"},
            {"H. Hall", "07123456814", "HALL1234567", "2028-01-18"},
            {"I. Allen", "07123456815", "ALLEN123456", "2028-02-22"},
            {"J. Young", "07123456816", "YOUNG123456", "2028-03-08"},
            {"K. King", "07123456817", "KING1234567", "2028-04-14"},
            // Leeds (6 drivers)
            {"L. Wright", "07123456818", "WRIGH123456", "2027-09-15"},
            {"M. Lopez", "07123456819", "LOPEZ123456", "2027-10-20"},
            {"N. Hill", "07123456820", "HILL1234567", "2027-11-10"},
            {"O. Scott", "07123456821", "SCOTT123456", "2027-12-05"},
            {"P. Green", "07123456822", "GREEN123456", "2028-01-12"},
            {"Q. Adams", "07123456823", "ADAMS123456", "2028-02-18"},
            // Bristol (5 drivers)
            {"R. Baker", "07123456824", "BAKER123456", "2027-10-15"},
            {"S. Nelson", "07123456825", "NELSO123456", "2027-11-20"},
            {"T. Carter", "07123456826", "CARTER123456", "2027-12-10"},
            {"U. Mitchell", "07123456827", "MITCH123456", "2028-01-05"},
            {"V. Perez", "07123456828", "PEREZ123456", "2028-02-12"}
        };
        
        int driverIndex = 0;
        int[] driversPerDepot = {12, 8, 9, 6, 5};
        
        for (int depotIndex = 0; depotIndex < depots.size() && depotIndex < driversPerDepot.length; depotIndex++) {
            Depot depot = depots.get(depotIndex);
            for (int i = 0; i < driversPerDepot[depotIndex] && driverIndex < driverData.length; i++) {
                Driver driver = new Driver();
                driver.setName(driverData[driverIndex][0]);
                driver.setContact(driverData[driverIndex][1]);
                driver.setLicenceNumber(driverData[driverIndex][2]);
                driver.setLicenceExpiry(LocalDate.parse(driverData[driverIndex][3]));
                driver.setShiftInfo("Full-time");
                driver.setDepot(depot);
                driver.setStatus("ACTIVE");
                driverRepository.save(driver);
                driverIndex++;
            }
        }
        
        log.info("Seeded {} drivers", driverRepository.count());
    }
    
    private void seedPostcodeRules(RouteRepository routeRepository, PostcodeRuleRepository postcodeRuleRepository, PostcodeRoutingService postcodeRoutingService) {
        List<Route> routes = routeRepository.findAll();
        LocalDate now = LocalDate.now();
        
        // Seed area-level rules for London routes
        Route routeA = routeRepository.findById("route-a").orElse(null);
        Route routeB = routeRepository.findById("route-b").orElse(null);
        Route routeC = routeRepository.findById("route-c").orElse(null);
        Route routeD = routeRepository.findById("route-d").orElse(null);
        Route routeE = routeRepository.findById("route-e").orElse(null);
        Route routeF = routeRepository.findById("route-f").orElse(null);
        
        if (routeA != null) {
            // SW area rules
            createPostcodeRule(postcodeRuleRepository, "SW", PostcodeRule.PostcodeLevel.AREA, routeA, now);
            // Some district rules
            createPostcodeRule(postcodeRuleRepository, "SW1", PostcodeRule.PostcodeLevel.DISTRICT, routeA, now);
            createPostcodeRule(postcodeRuleRepository, "SW2", PostcodeRule.PostcodeLevel.DISTRICT, routeA, now);
        }
        
        if (routeB != null) {
            // SE area rules
            createPostcodeRule(postcodeRuleRepository, "SE", PostcodeRule.PostcodeLevel.AREA, routeB, now);
            createPostcodeRule(postcodeRuleRepository, "SE1", PostcodeRule.PostcodeLevel.DISTRICT, routeB, now);
            createPostcodeRule(postcodeRuleRepository, "SE2", PostcodeRule.PostcodeLevel.DISTRICT, routeB, now);
        }
        
        if (routeC != null) {
            // EC area rules
            createPostcodeRule(postcodeRuleRepository, "EC", PostcodeRule.PostcodeLevel.AREA, routeC, now);
            createPostcodeRule(postcodeRuleRepository, "EC1", PostcodeRule.PostcodeLevel.DISTRICT, routeC, now);
            createPostcodeRule(postcodeRuleRepository, "EC2", PostcodeRule.PostcodeLevel.DISTRICT, routeC, now);
        }
        
        if (routeD != null) {
            // W and WC area rules
            createPostcodeRule(postcodeRuleRepository, "W", PostcodeRule.PostcodeLevel.AREA, routeD, now);
            createPostcodeRule(postcodeRuleRepository, "WC", PostcodeRule.PostcodeLevel.AREA, routeD, now);
            createPostcodeRule(postcodeRuleRepository, "W1", PostcodeRule.PostcodeLevel.DISTRICT, routeD, now);
            createPostcodeRule(postcodeRuleRepository, "WC1", PostcodeRule.PostcodeLevel.DISTRICT, routeD, now);
        }
        
        if (routeE != null) {
            // N area rules
            createPostcodeRule(postcodeRuleRepository, "N", PostcodeRule.PostcodeLevel.AREA, routeE, now);
            createPostcodeRule(postcodeRuleRepository, "N1", PostcodeRule.PostcodeLevel.DISTRICT, routeE, now);
            createPostcodeRule(postcodeRuleRepository, "N2", PostcodeRule.PostcodeLevel.DISTRICT, routeE, now);
        }
        
        if (routeF != null) {
            // E area rules
            createPostcodeRule(postcodeRuleRepository, "E", PostcodeRule.PostcodeLevel.AREA, routeF, now);
            createPostcodeRule(postcodeRuleRepository, "E1", PostcodeRule.PostcodeLevel.DISTRICT, routeF, now);
            createPostcodeRule(postcodeRuleRepository, "E2", PostcodeRule.PostcodeLevel.DISTRICT, routeF, now);
        }
        
        // Seed A-Z fallback rules distributed across routes
        char[] letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        int routesCount = routes.size();
        for (int i = 0; i < letters.length; i++) {
            Route route = routes.get(i % routesCount);
            postcodeRoutingService.seedFallbackRules(route);
        }
        
        log.info("Seeded {} postcode rules", postcodeRuleRepository.count());
    }
    
    private void createPostcodeRule(PostcodeRuleRepository repository, String pattern, PostcodeRule.PostcodeLevel level, Route route, LocalDate effectiveFrom) {
        PostcodeRule rule = new PostcodeRule();
        rule.setPattern(pattern);
        rule.setLevel(level);
        rule.setRoute(route);
        rule.setEffectiveFrom(effectiveFrom);
        rule.setEffectiveTo(null);
        repository.save(rule);
    }
    
    private void seedOrdersAndBoxes(RouteRepository routeRepository, OrderRepository orderRepository, BoxRepository boxRepository) {
        List<Route> routes = routeRepository.findAll();
        LocalDate now = LocalDate.now();
        
        // Create sample orders with boxes
        String[][] orderData = {
            {"ORD-4421", "DESP-4421", "123 High Street, London", "SW1A 1AA", "3"},
            {"ORD-4422", "DESP-4422", "456 Main Road, London", "SE1 1AA", "2"},
            {"ORD-4423", "DESP-4423", "789 Park Lane, London", "EC1A 1BB", "4"},
            {"ORD-4424", "DESP-4424", "321 Oxford Street, London", "W1A 1AA", "1"},
            {"ORD-4425", "DESP-4425", "654 King's Road, London", "N1 1AA", "3"},
            {"ORD-4426", "DESP-4426", "987 Commercial Street, London", "E1 1AA", "2"},
            {"ORD-4427", "DESP-4427", "111 Victoria Street, London", "SW1E 1AA", "5"},
            {"ORD-4428", "DESP-4428", "222 Baker Street, London", "W1U 1AA", "2"},
            {"ORD-4429", "DESP-4429", "333 Fleet Street, London", "EC4A 1AA", "3"},
            {"ORD-4430", "DESP-4430", "444 Brick Lane, London", "E1 6RU", "4"},
            {"ORD-4431", "DESP-4431", "555 Camden High Street, London", "NW1 7JE", "2"},
            {"ORD-4432", "DESP-4432", "666 Portobello Road, London", "W11 1AA", "3"},
            {"ORD-4433", "DESP-4433", "777 Whitechapel Road, London", "E1 1AA", "1"},
            {"ORD-4434", "DESP-4434", "888 Borough High Street, London", "SE1 1AA", "4"},
            {"ORD-4435", "DESP-4435", "999 Regent Street, London", "W1B 1AA", "2"},
            {"ORD-4436", "DESP-4436", "101 Piccadilly, London", "W1J 1AA", "3"},
            {"ORD-4437", "DESP-4437", "202 Strand, London", "WC2R 1AA", "2"},
            {"ORD-4438", "DESP-4438", "303 Tottenham Court Road, London", "W1T 1AA", "5"},
            {"ORD-4439", "DESP-4439", "404 Charing Cross Road, London", "WC2H 1AA", "2"},
            {"ORD-4440", "DESP-4440", "505 Marylebone High Street, London", "W1U 1AA", "3"}
        };
        
        for (String[] orderInfo : orderData) {
            Order order = new Order();
            order.setOrderId(orderInfo[0]);
            order.setDespatchId(orderInfo[1]);
            order.setCustomerAddress(orderInfo[2]);
            order.setDeliveryPostcode(orderInfo[3]);
            order.setOrderDate(now.minusDays(2));
            order.setRequestedDeliveryDate(now);
            order.setStatus("PENDING");
            // Route will be set by routing service when order is created
            orderRepository.save(order);
            
            int boxCount = Integer.parseInt(orderInfo[4]);
            for (int i = 1; i <= boxCount; i++) {
                Box box = new Box();
                box.setIdentifier(orderInfo[0] + "-BOX" + i);
                // Mix of statuses
                if (i == 1) {
                    box.setStatus(Box.BoxStatus.RECEIVED);
                    box.setReceivedAt(LocalDateTime.now().minusHours(2));
                } else if (i == 2 && boxCount > 2) {
                    box.setStatus(Box.BoxStatus.EXPECTED);
                } else {
                    box.setStatus(Box.BoxStatus.RECEIVED);
                    box.setReceivedAt(LocalDateTime.now().minusHours(1));
                }
                box.setOrder(order);
                boxRepository.save(box);
            }
        }
        
        log.info("Seeded {} orders and {} boxes", orderRepository.count(), boxRepository.count());
    }
    
    private void seedManifests(RouteRepository routeRepository, VehicleRepository vehicleRepository, DriverRepository driverRepository, BoxRepository boxRepository, ManifestRepository manifestRepository) {
        LocalDate today = LocalDate.now();
        List<Route> routes = routeRepository.findAll();
        
        if (routes.isEmpty()) {
            return;
        }
        
        // Get vehicles and drivers for first depot
        List<Vehicle> vehicles = vehicleRepository.findAll();
        List<Driver> drivers = driverRepository.findAll();
        
        if (vehicles.isEmpty() || drivers.isEmpty()) {
            return;
        }
        
        // Create manifests for first few routes
        Route routeA = routeRepository.findById("route-a").orElse(null);
        Route routeB = routeRepository.findById("route-b").orElse(null);
        Route routeC = routeRepository.findById("route-c").orElse(null);
        Route routeD = routeRepository.findById("route-d").orElse(null);
        Route routeE = routeRepository.findById("route-e").orElse(null);
        
        if (routeA != null && vehicles.size() > 0 && drivers.size() > 0) {
            Manifest manifest1 = new Manifest();
            manifest1.setRoute(routeA);
            manifest1.setDate(today);
            manifest1.setVehicle(vehicles.get(0));
            manifest1.setDriver(drivers.get(0));
            manifest1.setStatus(Manifest.ManifestStatus.COMPLETE);
            manifestRepository.save(manifest1);
            
            // Add some boxes to manifest
            List<Box> boxes = boxRepository.findAll();
            for (int i = 0; i < Math.min(10, boxes.size()); i++) {
                Box box = boxes.get(i);
                box.setManifest(manifest1);
                box.setStatus(Box.BoxStatus.DELIVERED);
                boxRepository.save(box);
            }
        }
        
        if (routeB != null && vehicles.size() > 1 && drivers.size() > 1) {
            Manifest manifest2 = new Manifest();
            manifest2.setRoute(routeB);
            manifest2.setDate(today);
            manifest2.setVehicle(vehicles.get(1));
            manifest2.setDriver(drivers.get(1));
            manifest2.setStatus(Manifest.ManifestStatus.IN_PROGRESS);
            manifestRepository.save(manifest2);
        }
        
        if (routeC != null && vehicles.size() > 2 && drivers.size() > 2) {
            Manifest manifest3 = new Manifest();
            manifest3.setRoute(routeC);
            manifest3.setDate(today);
            manifest3.setVehicle(vehicles.get(2));
            manifest3.setDriver(drivers.get(2));
            manifest3.setStatus(Manifest.ManifestStatus.IN_PROGRESS);
            manifestRepository.save(manifest3);
        }
        
        if (routeD != null && vehicles.size() > 3 && drivers.size() > 3) {
            Manifest manifest4 = new Manifest();
            manifest4.setRoute(routeD);
            manifest4.setDate(today);
            manifest4.setVehicle(vehicles.get(3));
            manifest4.setDriver(drivers.get(3));
            manifest4.setStatus(Manifest.ManifestStatus.CONFIRMED);
            manifestRepository.save(manifest4);
        }
        
        if (routeE != null && vehicles.size() > 4 && drivers.size() > 4) {
            Manifest manifest5 = new Manifest();
            manifest5.setRoute(routeE);
            manifest5.setDate(today);
            manifest5.setVehicle(vehicles.get(4));
            manifest5.setDriver(drivers.get(4));
            manifest5.setStatus(Manifest.ManifestStatus.CONFIRMED);
            manifestRepository.save(manifest5);
        }
        
        log.info("Seeded {} manifests", manifestRepository.count());
    }
}
