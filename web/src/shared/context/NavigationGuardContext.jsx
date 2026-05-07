import { createContext, useContext, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import ExitConfirmModal from '../components/ui/ExitConfirmModal';

const NavigationGuardContext = createContext({
  registerGuard: () => {},
  clearGuard: () => {},
});

export function NavigationGuardProvider({ children }) {
  const navigate = useNavigate();
  const [guard, setGuard] = useState(null); // { message, title, body }
  const [modal, setModal] = useState({ open: false, path: '' });

  const registerGuard = useCallback((config) => {
    setGuard(config); // { title, body }
  }, []);

  const clearGuard = useCallback(() => {
    setGuard(null);
  }, []);

  /* Called by sidebar/topbar links instead of navigate() */
  const guardedNavigate = useCallback((path) => {
    if (guard) {
      setModal({ open: true, path });
    } else {
      navigate(path);
    }
  }, [guard, navigate]);

  return (
    <NavigationGuardContext.Provider value={{ registerGuard, clearGuard, guardedNavigate, hasGuard: !!guard }}>
      {children}
      <ExitConfirmModal
        isOpen={modal.open}
        onCancel={() => setModal({ open: false, path: '' })}
        onConfirm={() => {
          const path = modal.path;
          setModal({ open: false, path: '' });
          clearGuard();
          navigate(path);
        }}
        title={guard?.title ?? 'Leave without saving?'}
        body={guard?.body ?? 'Your progress will be lost.'}
      />
    </NavigationGuardContext.Provider>
  );
}

export function useNavigationGuard() {
  return useContext(NavigationGuardContext);
}
