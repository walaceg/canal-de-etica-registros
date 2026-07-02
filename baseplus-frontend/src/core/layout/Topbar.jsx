import { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ChevronDown } from 'lucide-react';
import { BrandingLogo, Button } from '../../shared/components/index.js';
import { useBranding } from '../../shared/branding/useBranding.js';
import { useAuth } from '../auth/useAuth.js';
import { useTheme } from '../theme/useTheme.js';
import { AccountMenu } from './AccountMenu.jsx';
import { NavItem } from './NavItem.jsx';
import { getAuthorizedNavigationGroups } from './navigation.js';
import { useAuthorization } from '../auth/useAuthorization.js';

export function Topbar({ menuPrincipal = 'sidebar' }) {
  const { signOut, user } = useAuth();
  const { hasPermission } = useAuthorization();
  const { branding, assetVersion } = useBranding();
  const { menuPrincipal: activeMenuPrincipal, setMenuPrincipal } = useTheme();
  const navigate = useNavigate();
  const navigationMenuRef = useRef(null);
  const [userMenuOpen, setUserMenuOpen] = useState(false);
  const [openNavigationMenu, setOpenNavigationMenu] = useState(null);
  const showPrimaryNavigation = menuPrincipal === 'topbar';
  const compactBrandLogoUrl = branding.compactLogoUrl || branding.logoUrl;
  const navigationGroups = useMemo(() => getAuthorizedNavigationGroups(hasPermission), [hasPermission]);

  useEffect(() => {
    function handleClickOutside(event) {
      const clickedInsideNavigationMenu = navigationMenuRef.current && navigationMenuRef.current.contains(event.target);

      if (!clickedInsideNavigationMenu) {
        setOpenNavigationMenu(null);
      }
    }

    function handleEscape(event) {
      if (event.key === 'Escape') {
        setOpenNavigationMenu(null);
      }
    }

    document.addEventListener('mousedown', handleClickOutside);
    document.addEventListener('keydown', handleEscape);

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
      document.removeEventListener('keydown', handleEscape);
    };
  }, []);

  async function handleSignOut() {
    setUserMenuOpen(false);
    setOpenNavigationMenu(null);
    await signOut();
  }

  function goToAccount() {
    setUserMenuOpen(false);
    navigate('/app/conta');
  }

  function changeMenuPrincipal(mode) {
    setMenuPrincipal(mode);
    setUserMenuOpen(false);
    setOpenNavigationMenu(null);
  }

  function toggleNavigationMenu(menuTitle) {
    setUserMenuOpen(false);
    setOpenNavigationMenu((current) => (current === menuTitle ? null : menuTitle));
  }

  function closeNavigationMenu() {
    setOpenNavigationMenu(null);
  }

  return (
    <header className={['bp-topbar', showPrimaryNavigation ? 'bp-topbar--navigation' : ''].filter(Boolean).join(' ')}>
      <div className="bp-topbar__header-row">
        <div className="bp-topbar__left">
          <BrandingLogo
            className="bp-topbar__brand-logo"
            logoUrl={compactBrandLogoUrl}
            assetVersion={assetVersion}
            name={branding.nomePlataforma}
            subtitle={branding.subtituloInstitucional}
            showSubtitle={false}
            showText={false}
            size="sm"
            variant="mark"
          />
          <div className="bp-topbar__context">
            <span className="bp-topbar__title">
              <span>{branding.nomePlataforma}</span>
            </span>
            <span className="bp-topbar__subtitle">{branding.subtituloInstitucional}</span>
          </div>

          {showPrimaryNavigation ? (
            <div className="bp-topbar__navigation" ref={navigationMenuRef}>
              {navigationGroups.map((group) => {
                const GroupIcon = group.icon;
                const menuOpen = openNavigationMenu === group.title;

                return (
                  <div className="bp-topbar__admin" key={group.title}>
                    <Button
                      aria-expanded={menuOpen}
                      aria-haspopup="menu"
                      className="bp-topbar__admin-trigger"
                      variant="ghost"
                      onClick={() => toggleNavigationMenu(group.title)}
                    >
                      <span className="bp-topbar__admin-trigger-icon" aria-hidden="true">
                        <GroupIcon size={16} strokeWidth={2.2} />
                      </span>
                      <span className="bp-topbar__admin-trigger-label">{group.title}</span>
                      <ChevronDown className="bp-topbar__chevron" size={14} aria-hidden="true" />
                    </Button>

                    {menuOpen ? (
                      <div className="bp-topbar__admin-menu" role="menu" aria-label={group.title}>
                        <section className="bp-topbar__admin-section">
                          <p className="bp-topbar__admin-section-title">
                            <span className="bp-topbar__admin-section-icon" aria-hidden="true">
                              <GroupIcon size={12} strokeWidth={2.2} />
                            </span>
                            <span>{group.title}</span>
                          </p>
                          <div className="bp-topbar__admin-items">
                            {group.items.map((item) => (
                              <NavItem
                                key={item.to}
                                icon={item.icon}
                                label={item.label}
                                to={item.to}
                                variant="topbar"
                                onClick={closeNavigationMenu}
                              />
                            ))}
                          </div>
                        </section>
                      </div>
                    ) : null}
                  </div>
                );
              })}
            </div>
          ) : null}
        </div>

        <AccountMenu
          activeMenuPrincipal={activeMenuPrincipal}
          onChangeMenuPrincipal={changeMenuPrincipal}
          onOpenChange={(nextOpen) => {
            setUserMenuOpen(nextOpen);
            if (nextOpen) {
              setOpenNavigationMenu(null);
            }
          }}
          onRequestAccount={goToAccount}
          onRequestSignOut={handleSignOut}
          open={userMenuOpen}
          user={user}
        />
      </div>
    </header>
  );
}
