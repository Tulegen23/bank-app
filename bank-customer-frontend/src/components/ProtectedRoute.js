import React, { useContext } from 'react';
import { Navigate } from 'react-router-dom';
import { AuthContext } from './AuthContext';

function ProtectedRoute({ children, allowedRole }) {
  const { auth } = useContext(AuthContext);
  if (!auth.token || (allowedRole && auth.role !== allowedRole)) {
    return <Navigate to="/login" replace />;
  }
  return children;
}

export default ProtectedRoute