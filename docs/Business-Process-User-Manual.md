# Business Process User Manual
## Delivery Van Management System

**Version 1.0**  
March 2026

---

## Table of Contents

1. [Introduction](#introduction)
2. [Creating an Order](#creating-an-order)
3. [Receiving Goods](#receiving-goods)
4. [Manifesting Goods](#manifesting-goods)
5. [Troubleshooting](#troubleshooting)

---

## Introduction

This manual provides step-by-step instructions for the core business processes in the Delivery Van Management System:

- **Order Creation**: Entering new delivery orders into the system
- **Goods Receiving**: Checking in physical boxes as they arrive at the depot
- **Manifesting**: Creating delivery manifests to assign goods to vehicles and drivers

### Prerequisites

Before starting, ensure you have:
- Access to the system with appropriate user permissions
- Selected your working depot from the top navigation bar
- Required information ready (order details, box IDs, etc.)

### Key Concepts

- **Orders**: Represent customer delivery requests containing one or more boxes
- **Boxes**: Individual physical units that are tracked through the delivery lifecycle
- **Routes**: Delivery runs assigned to specific depots
- **Manifests**: Confirmed delivery runs that assign boxes to vehicles and drivers

---

## Creating an Order

### Overview

Orders can be created manually through the web interface or automatically via API integration. This section covers manual order entry.

### Step-by-Step Process

#### Step 1: Navigate to Order Entry

1. Log in to the system
2. Ensure your **Working Depot** is selected in the top navigation bar
3. Navigate to **Order Entry** from the main menu

> **Note**: Orders are automatically allocated to routes based on the delivery postcode. The route allocation is determined by your selected depot.

#### Step 2: Enter Order Identification

Fill in the required order identification fields:

- **Order ID** (required): Unique identifier for the order (e.g., `ORD-4521`)
- **Despatch ID** (required): Despatch identifier (e.g., `DSP-8834`)
- **Order Date** (required): Date the order was placed (format: YYYY-MM-DD)
- **Requested Delivery Date** (optional): Customer's requested delivery date

> **Important**: The combination of Order ID and Despatch ID must be unique. If an order with the same combination already exists, you will receive an error message.

#### Step 3: Enter Delivery Address

Complete the customer delivery address:

- **Customer / Company Name** (required): Name of the recipient
- **Address Line 1** (required): Primary address line
- **Address Line 2** (optional): Secondary address information
- **Town / City** (required): City or town name
- **Postcode** (required): UK postcode (e.g., `SW8 1RT`)

> **Tip**: As you type the postcode, the system automatically looks up the route allocation. You'll see a preview showing which route the order will be assigned to.

#### Step 4: Specify Box Details

Enter information about the boxes in the order:

- **Number of Boxes** (required): Total number of boxes expected for this order
- **Box ID Prefix** (optional): Prefix for box identifiers (e.g., `BOX-DSP8834-`)

If you provide a box prefix, the system will generate box IDs automatically:
- Example: With prefix `BOX-DSP8834-` and 3 boxes, IDs will be:
  - `BOX-DSP8834-001`
  - `BOX-DSP8834-002`
  - `BOX-DSP8834-003`

If no prefix is provided, boxes will be created without identifiers and can be assigned IDs later during receiving.

#### Step 5: Review Route Allocation

Before submitting, review the **Route Allocation Preview** panel on the right side:

- The system displays the postcode hierarchy matching process
- Shows which route the order will be allocated to
- Displays the depot name and route details
- Confirms readiness to submit

The route allocation uses a hierarchical matching system:
1. Full postcode match (most specific)
2. Sector match (e.g., `SW1A 1`)
3. District match (e.g., `SW1A`)
4. Area match (e.g., `SW`)
5. First letter fallback (e.g., `S`)

#### Step 6: Submit the Order

1. Review all entered information
2. Click **Submit Order** button
3. On success, you'll be redirected to the Dashboard
4. The order status will be **PENDING** until goods are received

### What Happens Next?

After order creation:
- The order is assigned to a route based on the delivery postcode
- Boxes are created with status **EXPECTED**
- The order appears in the "Orders Awaiting Goods" list
- The order is ready for goods receiving when physical boxes arrive

### Common Issues

**Issue**: "Order with Order ID X and Despatch ID Y already exists"
- **Solution**: Check if the order was previously created. Each Order ID + Despatch ID combination must be unique.

**Issue**: "Could not resolve postcode to a route"
- **Solution**: This should not occur due to A-Z fallback rules. Contact system administrator if this error appears.

---

## Receiving Goods

### Overview

When physical goods arrive at the depot, each box must be checked in individually. The system matches arriving boxes against expected boxes for orders.

### Step-by-Step Process

#### Step 1: Navigate to Goods Receiving

1. Ensure your **Working Depot** is selected
2. Navigate to **Goods Receiving** from the main menu
3. The page displays all orders awaiting goods for your depot

#### Step 2: View Orders Awaiting Goods

The left panel shows a table of orders awaiting goods:

- **Order ID**: Unique order identifier
- **Customer**: Customer name
- **Route**: Assigned route name
- **Boxes**: Progress indicator showing received vs. expected (e.g., `2 / 3`)

Color coding:
- 🟢 **Green**: All boxes received (`3 / 3 ✓`)
- 🟡 **Amber**: Partial receipt (`2 / 3`)
- 🔴 **Red**: No boxes received (`0 / 3`)

#### Step 3: Check In a Box

When a physical box arrives:

1. **Scan or Enter Box ID**: 
   - Use a barcode scanner, or
   - Manually type the box identifier in the "Box ID / Barcode" field
   - Press **Enter** or click **Check In**

2. **System Processing**:
   - The system matches the box ID to an expected box
   - Updates the box status from **EXPECTED** to **RECEIVED**
   - Records the timestamp of receipt
   - Updates the order's box count

3. **Confirmation**:
   - A success message appears showing the box ID and associated order
   - The orders list refreshes automatically
   - The box ID field clears for the next box

#### Step 4: Monitor Order Progress

Select an order from the list to view detailed information:

- **Order Details**: Customer name, route, and order ID
- **Box Status Grid**: Visual display of all boxes for the order
  - Green boxes: Received (shows timestamp)
  - Amber boxes: Still awaiting arrival
- **Progress Summary**: Shows which boxes will be manifested

#### Step 5: Handle Complete Orders

When all boxes for an order are received:

1. The order shows `X / X ✓` (all boxes received)
2. The order is ready to be marked for manifesting
3. Click **Ready for Manifest** button to mark the order as ready

#### Step 6: Handle Exceptions (Missing Boxes)

If boxes are missing or damaged:

1. Select the affected order
2. Click **Flag Exception** button
3. Enter a reason for the exception (e.g., "Box damaged in transit", "Box not received")
4. Click **Flag Exception** to confirm

> **Important**: Flagging an exception does not prevent manifesting. Available boxes can still be manifested, and missing boxes will be added to the next route run when they arrive.

### Box Status Lifecycle

Boxes progress through these statuses:

1. **EXPECTED**: Created when order is entered, awaiting physical arrival
2. **RECEIVED**: Physically checked in at the depot
3. **MANIFESTED**: Assigned to a manifest for delivery
4. **DELIVERED**: Successfully delivered to customer (future phase)

### Best Practices

- **Check in boxes immediately** upon arrival to maintain accurate inventory
- **Verify box IDs** match the order before checking in
- **Flag exceptions promptly** for missing or damaged boxes
- **Review order details** before marking as ready for manifest

### Common Issues

**Issue**: "Box not found"
- **Solution**: Verify the box ID is correct. Boxes must be created as part of an order first.

**Issue**: "Box has already been received"
- **Solution**: The box was previously checked in. Check the order details to confirm.

**Issue**: Box ID doesn't match any expected boxes
- **Solution**: Verify the box belongs to an order at your depot. Contact the order entry team if needed.

---

## Manifesting Goods

### Overview

Manifests assign received boxes to vehicles and drivers for delivery runs. A manifest represents a confirmed delivery plan for a specific route, date, vehicle, and driver.

### Step-by-Step Process

#### Step 1: Navigate to Manifest Builder

1. Ensure your **Working Depot** is selected
2. Navigate to **Manifest Builder** from the main menu
3. Select the route you want to create a manifest for (if multiple routes exist)

#### Step 2: Review Available Orders

The system automatically includes orders that are:
- Assigned to the selected route
- Marked as "Ready for Manifest"
- Have at least one received box

> **Note**: Orders with partial box receipt (some boxes still missing) can still be manifested. Available boxes will be included, and missing boxes will be added to the next run automatically.

#### Step 3: Configure Manifest Details

Fill in the manifest configuration:

- **Route**: Automatically set based on selection (cannot be changed)
- **Delivery Date**: Select the date for this delivery run (format: YYYY-MM-DD)
- **Driver** (required): Select from available drivers at your depot
- **Vehicle** (required): Select from available vehicles at your depot

> **Tip**: Ensure drivers and vehicles are available and not already assigned to another manifest for the same date.

#### Step 4: Review Delivery Stops

The right panel displays all delivery stops (orders) included in the manifest:

- **Order ID**: Unique order identifier
- **Address**: Customer delivery address
- **Boxes**: Number of boxes for this stop (e.g., `3 boxes` or `2 of 3 boxes`)
- **Status**: Box receipt status (Complete or Partial)

Color coding:
- **White background**: All boxes received
- **Amber background**: Partial box receipt

#### Step 5: Remove Stops (Optional)

If needed, you can remove orders from the manifest:

1. Click **Remove** next to the order you want to exclude
2. Confirm the removal
3. The order will be available for the next manifest

> **Note**: Only draft manifests can be modified. Once confirmed, manifests are locked.

#### Step 6: Save Draft (Optional)

Before confirming, you can save the manifest as a draft:

1. Click **Save Draft** button
2. Changes are saved but the manifest remains editable
3. You can return later to make adjustments

#### Step 7: Confirm Manifest

When ready to finalize:

1. Review all manifest details:
   - Route and date are correct
   - Driver and vehicle are assigned
   - All intended orders are included
   - Box counts are accurate

2. Click **Confirm Manifest** button

3. **System Processing**:
   - Manifest status changes from **DRAFT** to **CONFIRMED**
   - All included boxes are marked as **MANIFESTED**
   - Manifest becomes locked (cannot be modified without override)
   - Audit trail records the confirmation

4. **Confirmation**: Success message appears

> **Important**: Once confirmed, manifests cannot be edited. Changes require an audited override process. Double-check all details before confirming.

### Manifest Statuses

Manifests progress through these statuses:

1. **DRAFT**: Being created, can be modified
2. **CONFIRMED**: Finalized and locked, ready for vehicle loading
3. **IN_PROGRESS**: Vehicle has departed (future phase)
4. **COMPLETE**: All deliveries completed (future phase)

### Partial Box Receipt Handling

The system handles partial box receipt intelligently:

- **Available boxes are manifested**: Received boxes are included in the manifest
- **Missing boxes are tracked**: Expected but not yet received boxes remain linked to the order
- **Automatic allocation**: When missing boxes arrive, they're automatically allocated to the next route run
- **Exception flagging**: Orders with missing boxes can be flagged with exceptions

Example:
- Order has 3 expected boxes
- 2 boxes received, 1 still missing
- Manifest includes the 2 received boxes
- Missing box will be added to next route run when it arrives

### Best Practices

- **Verify driver availability** before assigning to manifest
- **Check vehicle capacity** matches box count
- **Review all stops** before confirming
- **Confirm manifests early** to allow time for vehicle loading
- **Handle exceptions** before manifesting when possible

### Common Issues

**Issue**: "Manifest already exists for route X on date Y"
- **Solution**: Only one manifest per route per day is allowed. Modify the existing manifest or choose a different date.

**Issue**: Driver or vehicle not available
- **Solution**: Ensure drivers and vehicles are properly configured in the system and not assigned to other manifests.

**Issue**: No orders available for manifesting
- **Solution**: Verify orders are marked as "Ready for Manifest" and have received boxes. Check that you're viewing the correct depot.

**Issue**: Cannot modify confirmed manifest
- **Solution**: Confirmed manifests are locked. Contact system administrator for override if changes are required.

---

## Troubleshooting

### General Issues

**Problem**: Cannot see orders or manifests
- **Check**: Ensure correct depot is selected in top navigation bar
- **Check**: Verify user permissions for the selected depot

**Problem**: Route allocation seems incorrect
- **Check**: Verify postcode is entered correctly
- **Check**: Review postcode routing rules in system configuration
- **Action**: Contact administrator to review routing rules

**Problem**: System is slow or unresponsive
- **Action**: Refresh the page
- **Action**: Check internet connection
- **Action**: Contact IT support if issue persists

### Order Creation Issues

**Problem**: Postcode not resolving to route
- **Action**: This should not occur due to A-Z fallback. Contact system administrator immediately.

**Problem**: Duplicate order error
- **Action**: Check if order was previously created. Use different Order ID or Despatch ID combination.

### Goods Receiving Issues

**Problem**: Box ID not recognized
- **Action**: Verify box ID matches expected format
- **Action**: Check that box belongs to an order at your depot
- **Action**: Contact order entry team to verify box was created

**Problem**: Box already received error
- **Action**: Box was previously checked in. Review order details to confirm status.

### Manifest Issues

**Problem**: Cannot create manifest for route
- **Check**: Ensure at least one order is ready for manifest
- **Check**: Verify route has received boxes
- **Action**: Mark orders as "Ready for Manifest" if needed

**Problem**: Cannot confirm manifest
- **Check**: All required fields are filled (driver, vehicle, date)
- **Check**: Manifest is in DRAFT status
- **Action**: Save draft first, then confirm

---

## Additional Resources

- **System Documentation**: See `README.md` for technical details
- **API Documentation**: See `delivery-api/README.md` for API integration
- **Product Requirements**: See `docs/DeliverySystem-PRD.md` for detailed system specifications

## Support

For additional assistance:
- Contact your depot manager
- Submit a support ticket through the system
- Refer to the system administrator for configuration issues

---

**Document Version**: 1.0  
**Last Updated**: March 2026  
**System Version**: Delivery Van Management System v1.0
