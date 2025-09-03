import React from 'react';
import {
  Drawer, List, ListItem, ListItemIcon, ListItemText, Toolbar, IconButton
} from '@mui/material';
import { Link } from 'react-router-dom';
import CloseIcon from '@mui/icons-material/Close';

const drawerWidth = 240;

export default function Sidebar({ mobileOpen, handleDrawerToggle, menuItems }) {
  const drawer = (
    <div>
      <Toolbar
        sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}
      >
        <span>Меню</span>
        <IconButton onClick={handleDrawerToggle}>
          <CloseIcon />
        </IconButton>
      </Toolbar>
      <List>
        {menuItems.map((item) => (
          <ListItem button key={item.label} component={Link} to={item.path} onClick={handleDrawerToggle}>
            <ListItemIcon>{item.icon}</ListItemIcon>
            <ListItemText primary={item.label} />
          </ListItem>
        ))}
      </List>
    </div>
  );

  return (
    <>
      <Drawer
        variant="temporary"
        open={mobileOpen}
        onClose={handleDrawerToggle}
        ModalProps={{ keepMounted: true }}
        sx={{
          display: { xs: 'block', sm: 'none' },
          '& .MuiDrawer-paper': { width: drawerWidth },
        }}
      >
        {drawer}
      </Drawer>

      <Drawer
        variant="persistent"
        open={mobileOpen}
        sx={{
          display: { xs: 'none', sm: 'block' },
          '& .MuiDrawer-paper': { width: drawerWidth },
        }}
      >
        {drawer}
      </Drawer>
    </>
  );
}
