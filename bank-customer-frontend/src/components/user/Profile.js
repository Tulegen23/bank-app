import React, { useState, useContext } from 'react';
import { AuthContext } from '../AuthContext';
import axios from 'axios';
import { Box, Typography, TextField, Button } from '@mui/material';

export default function Profile() {
  const { auth } = useContext(AuthContext);
  const [userId, setUserId] = useState('');
  const [data, setData] = useState({ fullName: '', phone: '' });

  const handleUpdate = async (e) => {
    e.preventDefault();
    await axios.put(`http://localhost:8081/api/customers/${userId}`, data, {
      headers: { Authorization: `Bearer ${auth.token}` },
    });
    alert('Данные обновлены');
  };

  return (
    <Box>
      <Typography variant="h5">Мои данные</Typography>
      <form onSubmit={handleUpdate}>
        <TextField label="ID" fullWidth margin="normal"
          value={userId} onChange={(e) => setUserId(e.target.value)}
        />
        <TextField label="ФИО" fullWidth margin="normal"
          value={data.fullName} onChange={(e) => setData({ ...data, fullName: e.target.value })}
        />
        <TextField label="Телефон" fullWidth margin="normal"
          value={data.phone} onChange={(e) => setData({ ...data, phone: e.target.value })}
        />
        <Button type="submit" variant="contained">Обновить</Button>
      </form>
    </Box>
  );
}
