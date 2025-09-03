import React, { useContext } from 'react';
import { AppBar, Toolbar, IconButton, Typography, Button } from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import { AuthContext } from '../AuthContext';

export default function Topbar({ handleDrawerToggle }) {
  const { auth, logout } = useContext(AuthContext);

  return (
    <AppBar position="fixed">
      <Toolbar>
        <IconButton
          color="inherit"
          aria-label="open drawer"
          edge="start"
          onClick={handleDrawerToggle}
          sx={{ mr: 2 }}
        >
          <MenuIcon />
        </IconButton>

        <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1 }}>
          Панель ({auth.role})
        </Typography>

        <Button color="inherit" onClick={logout}>
          Выйти
        </Button>
      </Toolbar>
    </AppBar>
  );
}
