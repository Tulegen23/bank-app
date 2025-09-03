import React, { useState, useContext } from 'react';
import { AuthContext } from '../AuthContext';
import axios from 'axios';
import { Box, Typography, TextField, Button, MenuItem } from '@mui/material';

export default function Accounts() {
  const { auth } = useContext(AuthContext);
  const [accountData, setAccountData] = useState({ clientId: '', currency: 'KZT' });
  const [accountListId, setAccountListId] = useState('');
  const [accounts, setAccounts] = useState([]);
  const [balanceData, setBalanceData] = useState({ accountNumber: '', amount: '', currency: 'KZT' });

  const handleCreate = async (e) => {
    e.preventDefault();
    await axios.post(
      `http://localhost:8082/api/accounts?clientId=${accountData.clientId}&currency=${accountData.currency}`,
      null, { headers: { Authorization: `Bearer ${auth.token}` } }
    );
    alert('Счет создан');
  };

  const handleList = async (e) => {
    e.preventDefault();
    const res = await axios.get(`http://localhost:8082/api/accounts/${accountListId}`, {
      headers: { Authorization: `Bearer ${auth.token}` },
    });
    setAccounts(res.data.data || []);
  };

  const handleBalance = async (e) => {
    e.preventDefault();
    await axios.post('http://localhost:8082/api/accounts/balance', balanceData, {
      headers: { Authorization: `Bearer ${auth.token}` },
    });
    alert('Баланс пополнен');
  };

  return (
    <Box>
      <Typography variant="h5">Счета клиентов</Typography>

      <form onSubmit={handleCreate}>
        <TextField label="ID клиента" margin="normal"
          value={accountData.clientId}
          onChange={(e) => setAccountData({ ...accountData, clientId: e.target.value })}
        />
        <TextField select label="Валюта" margin="normal"
          value={accountData.currency}
          onChange={(e) => setAccountData({ ...accountData, currency: e.target.value })}
        >
          <MenuItem value="KZT">KZT</MenuItem>
          <MenuItem value="USD">USD</MenuItem>
        </TextField>
        <Button type="submit" variant="contained">Создать</Button>
      </form>

      <form onSubmit={handleList}>
        <TextField label="ID клиента" margin="normal"
          value={accountListId}
          onChange={(e) => setAccountListId(e.target.value)}
        />
        <Button type="submit" variant="outlined">Показать счета</Button>
      </form>
      {accounts.map((acc, i) => (
        <Typography key={i}>№ {acc.accountNumber}, Баланс: {acc.balance}</Typography>
      ))}

      <form onSubmit={handleBalance}>
        <TextField label="Номер счета" margin="normal"
          value={balanceData.accountNumber}
          onChange={(e) => setBalanceData({ ...balanceData, accountNumber: e.target.value })}
        />
        <TextField label="Сумма" type="number" margin="normal"
          value={balanceData.amount}
          onChange={(e) => setBalanceData({ ...balanceData, amount: e.target.value })}
        />
        <Button type="submit" variant="contained">Пополнить</Button>
      </form>
    </Box>
  );
}
