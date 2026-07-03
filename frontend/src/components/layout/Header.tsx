import { Link, useNavigate } from 'react-router-dom';
import { useTheme } from '../../context/ThemeContext';
import { useAuth } from '../../hooks/useAuth';
import { Menu, Moon, Sun, LogOut } from 'lucide-react';
import { useState } from 'react';

export default function Header() {
  // Return null to prevent rendering duplicate navigation
  // The floating navigation in HomePage.tsx handles all navigation needs
  return null;
}
