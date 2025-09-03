import AppLayout from '../layout/AppLayout';
import { Routes, Route } from 'react-router-dom';
import Clients from './Clients';
import Accounts from './Accounts';
import Transfers from './Transfers';
import DashboardIcon from '@mui/icons-material/Dashboard';
import PeopleIcon from '@mui/icons-material/People';
import PaymentIcon from '@mui/icons-material/Payment';

export default function AdminDashboard() {
  const menuItems = [
    { label: 'Клиенты', path: '/admin/clients', icon: <PeopleIcon /> },
    { label: 'Счета', path: '/admin/accounts', icon: <DashboardIcon /> },
    { label: 'Переводы', path: '/admin/transfers', icon: <PaymentIcon /> },
  ];

  return (
    <AppLayout menuItems={menuItems}>
      <Routes>
        <Route path="clients" element={<Clients />} />
        <Route path="accounts" element={<Accounts />} />
        <Route path="transfers" element={<Transfers />} />
      </Routes>
    </AppLayout>
  );
}
