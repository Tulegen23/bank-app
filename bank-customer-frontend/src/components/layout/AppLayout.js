import React, { useState } from 'react';
import { Box, CssBaseline } from '@mui/material';
import Topbar from './Topbar';
import Sidebar from './Sidebar';

const drawerWidth = 240;

export default function AppLayout({ children, menuItems }) {
  const [mobileOpen, setMobileOpen] = useState(false);

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  return (
    <Box sx={{ display: 'flex' }}>
      <CssBaseline />
      <Topbar handleDrawerToggle={handleDrawerToggle} />
      <Sidebar
        mobileOpen={mobileOpen}
        handleDrawerToggle={handleDrawerToggle}
        menuItems={menuItems}
      />
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          p: 3,
          marginLeft: { sm: mobileOpen ? `${drawerWidth}px` : 0 }, // <-- отодвигаем контент
          transition: 'margin 0.3s ease',
        }}
      >
        {children}
      </Box>
    </Box>
  );
}
