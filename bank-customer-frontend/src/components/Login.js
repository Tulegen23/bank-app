import React, { useState, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from './AuthContext';
import axios from 'axios';
import {
  TextField,
  Button,
  Container,
  Typography,
  Box,
  Paper
} from '@mui/material';

function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const { login } = useContext(AuthContext);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.post('http://localhost:8081/api/login', { username, password });
      const token = response.data.token;
      const role = (username === 'admin') ? 'ADMIN' : 'USER';
      login(token, role);
      if (role === 'ADMIN') navigate('/admin');
      else navigate('/user');
    } catch (err) {
      setError('Неверное имя пользователя или пароль');
    }
  };

  return (
    <Box
      sx={{
        minHeight: '98vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        bgcolor: 'linear-gradient(135deg, #4A90E2 0%, #9013FE 100%)',
        background: 'linear-gradient(180deg, #4A90E2 0%, #9013FE 100%)',
      }}
    >
      <Container maxWidth="xs">
        <Paper
          elevation={6}
          sx={{
            p: 4,
            borderRadius: 3,
            textAlign: 'center',
            backdropFilter: 'blur(6px)',
          }}
        >
          <Typography
            variant="h4"
            component="h1"
            gutterBottom
            sx={{ fontWeight: 'bold', color: '#4A90E2' }}
          >
            Вход
          </Typography>

          <form onSubmit={handleSubmit}>
            <TextField
              label="Логин"
              fullWidth
              required
              margin="normal"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
            />
            <TextField
              label="Пароль"
              type="password"
              fullWidth
              required
              margin="normal"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
            {error && (
              <Typography color="error" sx={{ mt: 1 }}>
                {error}
              </Typography>
            )}
            <Button
              type="submit"
              variant="contained"
              fullWidth
              sx={{
                mt: 3,
                py: 1.2,
                borderRadius: 2,
                fontWeight: 'bold',
                fontSize: '16px',
                background: 'linear-gradient(135deg, #4A90E2 0%, #9013FE 100%)',
                '&:hover': {
                  background: 'linear-gradient(135deg, #357ABD 0%, #7513C8 100%)',
                },
              }}
            >
              Войти
            </Button>
          </form>
        </Paper>
      </Container>
    </Box>
  );
}

export default Login;
