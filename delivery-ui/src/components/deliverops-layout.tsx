import { Outlet } from 'react-router-dom';
import { ContextBar } from './context-bar';
import { DeliverOpsSidebar } from './deliverops-sidebar';

export function DeliverOpsLayout() {
  return (
    <div className="flex flex-col min-h-screen bg-gray-50">
      <ContextBar />
      <div className="flex flex-1 min-h-0">
        <DeliverOpsSidebar />
        <main className="flex-1 flex flex-col overflow-hidden min-w-0">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
