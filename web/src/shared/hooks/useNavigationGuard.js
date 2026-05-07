import { useEffect, useCallback, useRef } from 'react';
import { useNavigate, useBeforeUnload } from 'react-router-dom';

/**
 * Blocks in-app navigation and browser tab close when `isDirty` is true.
 * Returns `confirmNavigate(path)` — call this from your own modal's
 * "Yes, leave" button to actually perform the navigation.
 */
export function useNavigationGuard(isDirty) {
  const navigate = useNavigate();
  const pendingPath = useRef(null);

  /* Block browser refresh / tab close */
  useBeforeUnload(
    useCallback(
      (e) => {
        if (isDirty) {
          e.preventDefault();
          e.returnValue = '';
        }
      },
      [isDirty]
    )
  );

  /* Confirm and actually navigate */
  const confirmNavigate = useCallback(
    (path) => {
      if (path) navigate(path);
    },
    [navigate]
  );

  return { confirmNavigate };
}
