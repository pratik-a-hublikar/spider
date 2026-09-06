import { Box, Button, Paper, Stack, Typography } from '@mui/material';

const HomePage = () => (
  <Stack spacing={3}>
    <Box>
      <Typography variant="h4" fontWeight={700} gutterBottom>
        Home Page
      </Typography>
      <Typography variant="body1" color="text.secondary">
        Authentication is working. This page is intentionally kept minimal for the skeleton project.
      </Typography>
    </Box>

    <Paper variant="outlined" sx={{ p: 3 }}>
      <Stack spacing={2} alignItems="flex-start">
        <Typography variant="h6">Application Skeleton</Typography>
        <Typography variant="body2" color="text.secondary">
          Add future dashboards, menus, modules, and reducer-backed features here.
        </Typography>
        <Button variant="contained" disabled>
          Future Action
        </Button>
      </Stack>
    </Paper>
  </Stack>
);

export default HomePage;
