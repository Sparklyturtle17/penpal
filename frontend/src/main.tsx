import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import { AuthProvider } from './auth/auth';
import '@fontsource-variable/quicksand';      // default app font
import '@fontsource-variable/caveat';         // available as font-caveat (unused by default)
import '@fontsource/shadows-into-light';      // available as font-shadows (unused by default)
import './index.css';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <AuthProvider>
      <App />
    </AuthProvider>
  </React.StrictMode>,
);
