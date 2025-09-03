import React, { useState, useContext } from 'react';
import { AuthContext } from '../AuthContext';
import axios from 'axios';
import { Box, Typography, TextField, Button } from '@mui/material';

export default function UserAccounts() {
  const { auth } = useContext(AuthContext);
  const [userId, setUserId] = useState('');
  const [accounts, setAccounts] = useState([]);

  const handleList = async (e) => {
    e.preventDefault();
    const res = await axios.get(`http://localhost:8082/api/accounts/${userId}`, {
      headers: { Authorization: `Bearer ${auth.token}` },
    });
    setAccounts(res.data.data || []);
  };

  return (
    <Box>
      <Typography variant="h5">Мои счета</Typography>
      <form onSubmit={handleList}>
        <TextField label="ID" margin="normal"
          value={userId} onChange={(e) => setUserId(e.target.value)}
        />
        <Button type="submit" variant="contained">Показать</Button>
      </form>
      {accounts.map((a, i) => (
        <Typography key={i}>
          {a.accountNumber} – {a.currency}, {a.balance}
        </Typography>
      ))}
    </Box>
  );
}
