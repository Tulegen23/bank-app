import React, { useState, useContext } from 'react';
import { AuthContext } from '../AuthContext';
import axios from 'axios';
import { Box, Typography, TextField, Button, MenuItem } from '@mui/material';

export default function UserTransfers() {
  const { auth } = useContext(AuthContext);
  const [data, setData] = useState({
    fromAccountNumber: '', toAccountNumber: '', amount: '', currency: 'KZT', external: false,
  });

  const handleTransfer = async (e) => {
    e.preventDefault();
    await axios.post('http://localhost:8082/api/transfers', data, {
      headers: { Authorization: `Bearer ${auth.token}` },
    });
    alert('Перевод выполнен');
  };

  return (
    <Box>
      <Typography variant="h5">Переводы</Typography>
      <form onSubmit={handleTransfer}>
        <TextField label="Счет отправителя" fullWidth margin="normal"
          value={data.fromAccountNumber}
          onChange={(e) => setData({ ...data, fromAccountNumber: e.target.value })}
        />
        <TextField label="Счет получателя" fullWidth margin="normal"
          value={data.toAccountNumber}
          onChange={(e) => setData({ ...data, toAccountNumber: e.target.value })}
        />
        <TextField label="Сумма" type="number" fullWidth margin="normal"
          value={data.amount}
          onChange={(e) => setData({ ...data, amount: e.target.value })}
        />
        <TextField select label="Валюта" margin="normal"
          value={data.currency}
          onChange={(e) => setData({ ...data, currency: e.target.value })}
        >
          <MenuItem value="KZT">KZT</MenuItem>
          <MenuItem value="USD">USD</MenuItem>
        </TextField>
        <Button type="submit" variant="contained">Перевести</Button>
      </form>
    </Box>
  );
}
