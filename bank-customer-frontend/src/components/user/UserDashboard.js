import AppLayout from '../layout/AppLayout';
import { Routes, Route } from 'react-router-dom';
import Profile from './Profile';
import UserAccounts from './UserAccounts';
import UserTransfers from './UserTransfers';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import DashboardIcon from '@mui/icons-material/Dashboard';
import PaymentIcon from '@mui/icons-material/Payment';

export default function UserDashboard() {
  const menuItems = [
    { label: 'Мои данные', path: '/user/profile', icon: <AccountCircleIcon /> },
    { label: 'Мои счета', path: '/user/accounts', icon: <DashboardIcon /> },
    { label: 'Переводы', path: '/user/transfers', icon: <PaymentIcon /> },
  ];

  return (
    <AppLayout menuItems={menuItems}>
      <Routes>
        <Route path="profile" element={<Profile />} />
        <Route path="accounts" element={<UserAccounts />} />
        <Route path="transfers" element={<UserTransfers />} />
      </Routes>
    </AppLayout>
  );
}
