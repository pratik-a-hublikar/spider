import { useState } from 'react';
import { IconButton, InputAdornment, SvgIcon, TextField, Tooltip } from '@mui/material';

const VisibilityIcon = () => (
  <SvgIcon fontSize="small">
    <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5C21.27 7.61 17 4.5 12 4.5Zm0 12.5a5 5 0 1 1 0-10 5 5 0 0 1 0 10Zm0-8a3 3 0 1 0 0 6 3 3 0 0 0 0-6Z" />
  </SvgIcon>
);

const VisibilityOffIcon = () => (
  <SvgIcon fontSize="small">
    <path d="m2.1 3.51 2.66 2.66A11.8 11.8 0 0 0 1 12c1.73 4.39 6 7.5 11 7.5 1.79 0 3.48-.4 4.97-1.1l3.52 3.51 1.41-1.41L3.51 2.1 2.1 3.51ZM12 17a5 5 0 0 1-5-5c0-.66.13-1.29.36-1.86l1.55 1.55a3 3 0 0 0 3.4 3.4l1.55 1.55c-.57.23-1.2.36-1.86.36Zm0-12.5c5 0 9.27 3.11 11 7.5a11.8 11.8 0 0 1-3.04 4.47l-2.78-2.78A5 5 0 0 0 10.31 6.82L8.15 4.66A11.8 11.8 0 0 1 12 4.5Zm2.97 6.55-4.02-4.02A5 5 0 0 1 16.97 13l-2-1.95Z" />
  </SvgIcon>
);

const PasswordField = ({ slotProps, ...props }) => {
  const [visible, setVisible] = useState(false);
  const actionLabel = visible ? 'Hide password' : 'Show password';

  return (
    <TextField
      {...props}
      type={visible ? 'text' : 'password'}
      slotProps={{
        ...slotProps,
        input: {
          ...slotProps?.input,
          endAdornment: (
            <InputAdornment position="end">
              <Tooltip title={actionLabel}>
                <IconButton
                  type="button"
                  edge="end"
                  aria-label={actionLabel}
                  onClick={() => setVisible((current) => !current)}
                  onMouseDown={(event) => event.preventDefault()}
                >
                  {visible ? <VisibilityOffIcon /> : <VisibilityIcon />}
                </IconButton>
              </Tooltip>
            </InputAdornment>
          ),
        },
      }}
    />
  );
};

export default PasswordField;
