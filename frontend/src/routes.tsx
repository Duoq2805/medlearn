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
import ReviewerDashboardPage from './pages/reviewer/ReviewerDashboardPage';
import AdminDashboardPage from './pages/admin/AdminDashboardPage';
import FlashcardsPage from './pages/flashcards/FlashcardsPage';
import QuizPage from './pages/quiz/QuizPage';
import BookmarkPage from './pages/bookmarks/BookmarkPage';
import HistoryPage from './pages/history/HistoryPage';
import ProgressPage from './pages/progress/ProgressPage';
import ProfilePage from './pages/settings/ProfilePage';
import ThemingDashboardPage from './pages/settings/ThemingDashboardPage';
import NotFoundPage from './pages/NotFoundPage';

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
      { path: 'reviewer', element: <ReviewerDashboardPage /> },
      { path: 'admin', element: <AdminDashboardPage /> },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
]);

export default router;
