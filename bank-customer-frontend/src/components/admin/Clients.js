import React, { useState, useContext } from 'react';
import { AuthContext } from '../AuthContext';
import axios from 'axios';
import { Box, Typography, TextField, Button } from '@mui/material';

export default function Clients() {
  const { auth } = useContext(AuthContext);

  const [regData, setRegData] = useState({ fullName: '', iin: '', phone: '' });
  const [updateId, setUpdateId] = useState('');
  const [updateData, setUpdateData] = useState({ fullName: '', phone: '' });
  const [activityId, setActivityId] = useState('');
  const [activityData, setActivityData] = useState([]);

  const handleReg = async (e) => {
    e.preventDefault();
    try {
      await axios.post('http://localhost:8081/api/customers', regData, {
        headers: { Authorization: `Bearer ${auth.token}` },
      });
      alert('Клиент создан');
    } catch {
      alert('Ошибка создания клиента');
    }
  };

  const handleUpdate = async (e) => {
    e.preventDefault();
    try {
      await axios.put(`http://localhost:8081/api/customers/${updateId}`, updateData, {
        headers: { Authorization: `Bearer ${auth.token}` },
      });
      alert('Данные обновлены');
    } catch {
      alert('Ошибка обновления');
    }
  };

  const handleGetActivity = async (e) => {
    e.preventDefault();
    try {
      const res = await axios.get(`http://localhost:8081/api/customers/${activityId}/activities`, {
        headers: { Authorization: `Bearer ${auth.token}` },
      });
      setActivityData(res.data.data || []);
    } catch {
      alert('Ошибка получения активности');
    }
  };

  return (
    <Box>
      <Typography variant="h5">Управление клиентами</Typography>

      <form onSubmit={handleReg}>
        <TextField label="ФИО" fullWidth margin="normal"
          value={regData.fullName}
          onChange={(e) => setRegData({ ...regData, fullName: e.target.value })}
        />
        <TextField label="ИИН" fullWidth margin="normal"
          value={regData.iin}
          onChange={(e) => setRegData({ ...regData, iin: e.target.value })}
        />
        <TextField label="Телефон" fullWidth margin="normal"
          value={regData.phone}
          onChange={(e) => setRegData({ ...regData, phone: e.target.value })}
        />
        <Button type="submit" variant="contained">Создать клиента</Button>
      </form>

      <form onSubmit={handleUpdate}>
        <TextField label="ID клиента" fullWidth margin="normal"
          value={updateId}
          onChange={(e) => setUpdateId(e.target.value)}
        />
        <TextField label="Новое ФИО" fullWidth margin="normal"
          value={updateData.fullName}
          onChange={(e) => setUpdateData({ ...updateData, fullName: e.target.value })}
        />
        <TextField label="Новый телефон" fullWidth margin="normal"
          value={updateData.phone}
          onChange={(e) => setUpdateData({ ...updateData, phone: e.target.value })}
        />
        <Button type="submit" variant="contained">Обновить</Button>
      </form>

      <form onSubmit={handleGetActivity}>
        <TextField label="ID клиента для активности" fullWidth margin="normal"
          value={activityId}
          onChange={(e) => setActivityId(e.target.value)}
        />
        <Button type="submit" variant="outlined">Показать активность</Button>
      </form>

      <ul>
        {activityData.map((a, i) => (
          <li key={i}>{a.actionType} – {a.details}</li>
        ))}
      </ul>
    </Box>
  );
}
