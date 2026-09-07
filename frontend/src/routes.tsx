import {
  createBrowserRouter,
  Outlet,
  ScrollRestoration,
} from 'react-router-dom';
import App from './App';
import HomePage from './pages/home/HomePage';

import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';
import UserDashboardPage from './pages/dashboard/UserDashboardPage';
import DiseaseExplorerPage from './pages/explorer/DiseaseExplorerPage';
import DiseaseDetailPage from './pages/disease/DiseaseDetailPage';
import CreateDiseasePage from './pages/disease/CreateDiseasePage';
import EditDiseasePage from './pages/disease/EditDiseasePage';
import MyDraftsPage from './pages/disease/MyDraftsPage';
import SymptomCheckerPage from './pages/symptom-checker/SymptomCheckerPage';
import CaseStudyListPage from './pages/cases/CaseStudyListPage';
import CaseStudyDetailPage from './pages/cases/CaseStudyDetailPage';
import FlashcardsPage from './pages/flashcards/FlashcardsPage';
import QuizPage from './pages/quiz/QuizPage';
import BookmarkPage from './pages/bookmarks/BookmarkPage';
import HistoryPage from './pages/history/HistoryPage';
import ProgressPage from './pages/progress/ProgressPage';
import ProfilePage from './pages/settings/ProfilePage';
import ThemingDashboardPage from './pages/settings/ThemingDashboardPage';
import NotFoundPage from './pages/NotFoundPage';

// Reviewer pages
import ReviewerDashboardPage from './pages/reviewer/ReviewerDashboardPage';
import ReviewerQueuePage from './pages/reviewer/ReviewerQueuePage';
import ReviewerReviewPage from './pages/reviewer/ReviewerReviewPage';
import ReviewerHistoryPage from './pages/reviewer/ReviewerHistoryPage';
import ReviewerReportsPage from './pages/reviewer/ReviewerReportsPage';

// Admin pages
import AdminDashboardPage from './pages/admin/AdminDashboardPage';
import AdminUsersPage from './pages/admin/AdminUsersPage';
import AdminDiseasesPage from './pages/admin/AdminDiseasesPage';
import AdminCategoriesPage from './pages/admin/AdminCategoriesPage';
import AdminSymptomsPage from './pages/admin/AdminSymptomsPage';
import AdminCasesPage from './pages/admin/AdminCasesPage';
import AdminReportsPage from './pages/admin/AdminReportsPage';
import AdminAuditLogsPage from './pages/admin/AdminAuditLogsPage';
import AdminAiPage from './pages/admin/AdminAiPage';

const router = createBrowserRouter([
  {
    path: '/',
    element: (
      <>
        <ScrollRestoration />
        <App />
      </>
    ),
    children: [
      { index: true, element: <HomePage /> },
      { path: 'login', element: <LoginPage /> },
      { path: 'register', element: <RegisterPage /> },
      // USER workspace
      { path: 'dashboard', element: <UserDashboardPage /> },
      { path: 'explorer', element: <DiseaseExplorerPage /> },
      { path: 'disease/:id', element: <DiseaseDetailPage /> },
      { path: 'disease/:id/edit', element: <EditDiseasePage /> },
      { path: 'diseases/new', element: <CreateDiseasePage /> },
      { path: 'drafts', element: <MyDraftsPage /> },
      { path: 'symptom-checker', element: <SymptomCheckerPage /> },
      { path: 'cases', element: <CaseStudyListPage /> },
      { path: 'case/:id', element: <CaseStudyDetailPage /> },
      { path: 'flashcards', element: <FlashcardsPage /> },
      { path: 'quiz', element: <QuizPage /> },
      { path: 'bookmarks', element: <BookmarkPage /> },
      { path: 'history', element: <HistoryPage /> },
      { path: 'progress', element: <ProgressPage /> },
      { path: 'profile', element: <ProfilePage /> },
      { path: 'settings', element: <ThemingDashboardPage /> },
      // REVIEWER workspace
      { path: 'reviewer/dashboard', element: <ReviewerDashboardPage /> },
      { path: 'reviewer/queue', element: <ReviewerQueuePage /> },
      { path: 'reviewer/review/:id', element: <ReviewerReviewPage /> },
      { path: 'reviewer/history', element: <ReviewerHistoryPage /> },
      { path: 'reviewer/reports', element: <ReviewerReportsPage /> },
      // ADMIN workspace
      { path: 'admin/dashboard', element: <AdminDashboardPage /> },
      { path: 'admin/users', element: <AdminUsersPage /> },
      { path: 'admin/diseases', element: <AdminDiseasesPage /> },
      { path: 'admin/categories', element: <AdminCategoriesPage /> },
      { path: 'admin/symptoms', element: <AdminSymptomsPage /> },
      { path: 'admin/cases', element: <AdminCasesPage /> },
      { path: 'admin/reports', element: <AdminReportsPage /> },
      { path: 'admin/audit-logs', element: <AdminAuditLogsPage /> },
      { path: 'admin/ai', element: <AdminAiPage /> },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
]);

export default router;
