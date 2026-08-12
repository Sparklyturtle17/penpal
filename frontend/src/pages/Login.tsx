import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/auth';

const MODE = import.meta.env.VITE_AUTH_MODE ?? 'dev';

export default function Login() {
  const { login } = useAuth();
  const nav = useNavigate();
  const [username, setUsername] = useState('monitor');
  const [password, setPassword] = useState('monitor');

  if (MODE === 'auth0') {
    return (
      <div className="grid min-h-screen place-items-center bg-navy-50">
        <button
          onClick={() => login()}
          className="rounded-lg bg-indigo-600 px-5 py-2.5 font-medium text-white hover:bg-indigo-700"
        >
          Log in with Auth0
        </button>
      </div>
    );
  }

  return (
    <div className="grid min-h-screen place-items-center bg-navy-50">
      <form
        onSubmit={(e) => {
          e.preventDefault();
          login({ username, password });
          nav('/');
        }}
        className="w-80 space-y-3 rounded-xl bg-white p-6 shadow"
      >
        <h1 className="text-lg font-semibold">Penpals — dev login</h1>
        <input
          className="w-full rounded border px-3 py-2"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          placeholder="username"
        />
        <input
          className="w-full rounded border px-3 py-2"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="password"
        />
        <button className="w-full rounded bg-indigo-600 px-3 py-2 font-medium text-white hover:bg-indigo-700">
          Sign in
        </button>
        <p className="text-xs text-navy-500">
          dev users: penpal / parent_helper / monitor / admin (password = username)
        </p>
      </form>
    </div>
  );
}
