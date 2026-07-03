import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { useAuth } from '../../hooks/useAuth';
import { Link, useNavigate } from 'react-router-dom';
import { ChevronRight, Eye, EyeOff, AlertCircle } from 'lucide-react';
import { MedvoraLogo } from '../../components/MedvoraLogo';

const registerSchema = z.object({
  username: z.string().min(3, 'Username must be at least 3 characters'),
  email: z.string().email('Invalid email address'),
  password: z.string().min(6, 'Password must be at least 6 characters'),
  confirmPassword: z.string(),
  fullName: z.string().min(2, 'Full name is required'),
}).refine((data) => data.password === data.confirmPassword, {
  message: 'Passwords do not match',
  path: ['confirmPassword'],
});

type RegisterFormValues = z.infer<typeof registerSchema>;

export default function RegisterPage() {
  const { register: registerUser, registerError, registerLoading } = useAuth();
  const navigate = useNavigate();
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const { register, handleSubmit, formState: { errors } } = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      username: '',
      email: '',
      password: '',
      confirmPassword: '',
      fullName: '',
    }
  });

  const onSubmit = (data: RegisterFormValues) => {
    registerUser({
      username: data.username,
      email: data.email,
      password: data.password,
      fullName: data.fullName,
    });
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
              Create Your Account
            </h1>
            <p className="text-[var(--text-secondary)]">
              Join Medvora and start learning smarter today
            </p>
          </div>

          {registerError && (
            <div className="flex items-start gap-2 p-3 rounded-lg bg-red-500/10 text-red-600 dark:text-red-400 text-sm" role="alert">
              <AlertCircle size={16} className="mt-0.5 shrink-0" />
              <span>{registerError}</span>
            </div>
          )}

          <form className="space-y-5" onSubmit={handleSubmit(onSubmit)}>
            <div className="space-y-2">
              <label htmlFor="fullName" className="block text-sm font-medium text-[var(--text-primary)]">
                Full Name
              </label>
              <input
                id="fullName"
                type="text"
                autoComplete="name"
                autoFocus
                placeholder="Your full name"
                className="input-neumorphic"
                disabled={registerLoading}
                {...register('fullName')}
              />
              {errors.fullName && (
                <p className="text-sm text-red-600">{errors.fullName.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <label htmlFor="username" className="block text-sm font-medium text-[var(--text-primary)]">
                Username
              </label>
              <input
                id="username"
                type="text"
                autoComplete="username"
                placeholder="Your username"
                className="input-neumorphic"
                disabled={registerLoading}
                {...register('username')}
              />
              {errors.username && (
                <p className="text-sm text-red-600">{errors.username.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <label htmlFor="email" className="block text-sm font-medium text-[var(--text-primary)]">
                Email Address
              </label>
              <input
                id="email"
                type="email"
                autoComplete="email"
                placeholder="you@example.com"
                className="input-neumorphic"
                disabled={registerLoading}
                {...register('email')}
              />
              {errors.email && (
                <p className="text-sm text-red-600">{errors.email.message}</p>
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
                  autoComplete="new-password"
                  placeholder="Your password"
                  className="input-neumorphic pr-12"
                  disabled={registerLoading}
                  {...register('password')}
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  disabled={registerLoading}
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

            <div className="space-y-2">
              <label htmlFor="confirmPassword" className="block text-sm font-medium text-[var(--text-primary)]">
                Confirm Password
              </label>
              <div className="relative">
                <input
                  id="confirmPassword"
                  type={showConfirmPassword ? 'text' : 'password'}
                  autoComplete="new-password"
                  placeholder="Confirm your password"
                  className="input-neumorphic pr-12"
                  disabled={registerLoading}
                  {...register('confirmPassword')}
                />
                <button
                  type="button"
                  onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  disabled={registerLoading}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-[var(--text-secondary)] hover:text-[var(--accent-primary)] transition-colors p-1"
                  aria-label={showConfirmPassword ? 'Hide password' : 'Show password'}
                >
                  {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
              {errors.confirmPassword && (
                <p className="text-sm text-red-600">{errors.confirmPassword.message}</p>
              )}
            </div>

            <button
              type="submit"
              disabled={registerLoading}
              className="btn-neumorphic-primary w-full py-3 text-base inline-flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {registerLoading ? 'Creating Account...' : 'Create Account'}
              <ChevronRight size={18} />
            </button>
          </form>

          <p className="text-center text-sm text-[var(--text-secondary)]">
            Already have an account?{' '}
            <Link to="/login" className="font-medium text-[var(--accent-primary)] hover:text-[#a8c66a] transition-colors">
              Sign in
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
