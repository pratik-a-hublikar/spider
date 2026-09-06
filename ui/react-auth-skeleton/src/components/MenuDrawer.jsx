import { useEffect, useState } from 'react';
import { Alert, Box, CircularProgress, Collapse, Divider, Drawer, List, ListItemButton, ListItemText, Toolbar, Typography } from '@mui/material';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { fetchMenu } from '../features/menu/menuSlice';

const getChildren = (item) => item.subModules ?? [];
const getKey = (item, index) => `${item.name}-${item.url}-${index}`;

const MenuItems = ({ items, depth = 0, onNavigate }) => {
  const [openItems, setOpenItems] = useState({});

  return items.map((item, index) => {
    const key = getKey(item, index);
    const children = getChildren(item);
    const hasChildren = Array.isArray(children) && children.length > 0;
    const isOpen = Boolean(openItems[key]);

    const handleClick = () => {
      if (hasChildren) {
        setOpenItems((current) => ({ ...current, [key]: !current[key] }));
      } else {
        onNavigate(item.url);
      }
    };

    return (
      <Box key={key}>
        <ListItemButton onClick={handleClick} sx={{ pl: 2 + depth * 2 }}>
          <ListItemText primary={item.name} />
          {hasChildren && <Typography component="span">{isOpen ? '−' : '+'}</Typography>}
        </ListItemButton>
        {hasChildren && (
          <Collapse in={isOpen} timeout="auto" unmountOnExit>
            <List disablePadding>
              <MenuItems items={children} depth={depth + 1} onNavigate={onNavigate} />
            </List>
          </Collapse>
        )}
      </Box>
    );
  });
};

const MenuDrawer = ({ open, onClose }) => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { items, status, error } = useSelector((state) => state.menu);

  useEffect(() => {
    if (status === 'idle') {
      dispatch(fetchMenu());
    }
  }, [dispatch, status]);

  const handleNavigate = (url) => {
    if (!url) return;
    if (/^https?:\/\//i.test(url)) window.location.assign(url);
    else navigate(url.startsWith('/') ? url : `/${url}`);
    onClose();
  };

  return (
    <Drawer open={open} onClose={onClose} slotProps={{ paper: { sx: { width: 280 } } }}>
      <Toolbar><Typography variant="h6">Menu</Typography></Toolbar>
      <Divider />
      {status === 'loading' && <Box sx={{ display: 'grid', placeItems: 'center', py: 4 }}><CircularProgress size={28} /></Box>}
      {status === 'failed' && <Alert severity="error">{error}</Alert>}
      {status === 'succeeded' && items.length === 0 && <Typography color="text.secondary" sx={{ p: 2 }}>No menu access is available.</Typography>}
      <List><MenuItems items={items} onNavigate={handleNavigate} /></List>
    </Drawer>
  );
};

export default MenuDrawer;
