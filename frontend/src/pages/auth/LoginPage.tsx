import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { useAuth } from '../../hooks/useAuth';
import { Link } from 'react-router-dom';
import { ChevronRight, Eye, EyeOff, AlertCircle } from 'lucide-react';
import { MedvoraLogo } from '../../components/MedvoraLogo';

const loginSchema = z.object({
  usernameOrEmail: z.string().min(1, 'Email or username is required'),
  password: z.string().min(1, 'Password is required'),
});

type LoginFormValues = z.infer<typeof loginSchema>;

export default function LoginPage() {
  const { login, loginError, loginLoading } = useAuth();
  const [showPassword, setShowPassword] = useState(false);

  const { register, handleSubmit, formState: { errors } } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      usernameOrEmail: '',
      password: '',
    }
  });

  const onSubmit = (data: LoginFormValues) => {
    login({ email: data.usernameOrEmail, password: data.password });
  };

  return (
    <div className="min-h-screen flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8 bg-[var(--bg-primary)] relative z-10">
      <div className="w-full max-w-md">
        <div className="card-neumorphic space-y-8 p-8">
          <div className="text-center space-y-3">
            <div className="inline-flex justify-center">
              <MedvoraLogo className="h-14 w-14" variant="icon" />
            </div>
            <h1 className="font-display text-4xl font-bold text-[var(--text-primary)]">
              Welcome Back
            </h1>
            <p className="text-[var(--text-secondary)]">
              Sign in to continue your medical learning journey
            </p>
          </div>

          {loginError && (
            <div className="flex items-start gap-2 p-3 rounded-lg bg-red-500/10 text-red-600 dark:text-red-400 text-sm" role="alert">
              <AlertCircle size={16} className="mt-0.5 shrink-0" />
              <span>{loginError}</span>
            </div>
          )}

          <form className="space-y-5" onSubmit={handleSubmit(onSubmit)}>
            <div className="space-y-2">
              <label htmlFor="usernameOrEmail" className="block text-sm font-medium text-[var(--text-primary)]">
                Email or Username
              </label>
              <input
                id="usernameOrEmail"
                type="text"
                autoComplete="username"
                autoFocus
                placeholder="you@example.com or username"
                className="input-neumorphic"
                disabled={loginLoading}
                {...register('usernameOrEmail')}
              />
              {errors.usernameOrEmail && (
                <p className="text-sm text-red-600">{errors.usernameOrEmail.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <label htmlFor="password" className="block text-sm font-medium text-[var(--text-primary)]">
                Password
              </label>
              <div className="relative">
                <input
                  id="password"
                  type={showPassword ? 'text' : 'password'}
                  autoComplete="current-password"
                  placeholder="••••••••"
                  className="input-neumorphic pr-12"
                  disabled={loginLoading}
                  {...register('password')}
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  disabled={loginLoading}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-[var(--text-secondary)] hover:text-[var(--accent-primary)] transition-colors p-1"
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                >
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
              {errors.password && (
                <p className="text-sm text-red-600">{errors.password.message}</p>
              )}
            </div>

            <div className="flex items-center justify-between pt-2">
              <div className="flex items-center gap-2">
                <input
                  id="remember-me"
                  type="checkbox"
                  className="w-4 h-4 rounded border-2 border-[#8cb04d] checked:bg-[var(--accent-primary)] cursor-pointer"
                />
                <label htmlFor="remember-me" className="text-sm text-[var(--text-secondary)]">
                  Remember me
                </label>
              </div>
              <Link to="/forgot-password" className="text-sm font-medium text-[var(--accent-primary)] hover:text-[#a8c66a] transition-colors">
                Forgot password?
              </Link>
            </div>

            <button
              type="submit"
              disabled={loginLoading}
              className="btn-neumorphic-primary w-full py-3 mt-6 inline-flex items-center justify-center gap-2 group disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loginLoading ? 'Signing in...' : 'Sign In'}
              {!loginLoading && <ChevronRight size={18} className="group-hover:translate-x-1 transition-transform" />}
            </button>
          </form>

          <div className="relative">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-[var(--shadow-dark)]" />
            </div>
            <div className="relative flex justify-center text-sm">
              <span className="px-2 bg-[var(--bg-primary)] text-[var(--text-secondary)]">
                Or continue with
              </span>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <button className="btn-neumorphic-secondary py-3 text-sm font-medium">
              Google
            </button>
            <button className="btn-neumorphic-secondary py-3 text-sm font-medium">
              GitHub
            </button>
          </div>
        </div>

        <div className="mt-8 text-center">
          <p className="text-[var(--text-secondary)]">
            Don't have an account?{' '}
            <Link to="/register" className="font-medium text-[var(--accent-primary)] hover:text-[#a8c66a] transition-colors">
              Sign up
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
