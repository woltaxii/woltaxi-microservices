// Modern UI/UX Stil Kütüphanesi - WOLTAXI
export const modernTheme = {
  // Color Palette - 2024 Trend Colors
  colors: {
    // Primary Brand Colors
    primary: {
      50: '#F5F3FF',
      100: '#EDE9FE', 
      200: '#DDD6FE',
      300: '#C4B5FD',
      400: '#A78BFA',
      500: '#8B5CF6', // Ana marka rengi
      600: '#7C3AED',
      700: '#6D28D9',
      800: '#5B21B6',
      900: '#4C1D95'
    },
    
    // Success Colors (Emerald)
    success: {
      50: '#ECFDF5',
      100: '#D1FAE5',
      200: '#A7F3D0',
      300: '#6EE7B7',
      400: '#34D399',
      500: '#10B981', // Ana success rengi
      600: '#059669',
      700: '#047857',
      800: '#065F46',
      900: '#064E3B'
    },
    
    // Warning Colors (Amber)
    warning: {
      50: '#FFFBEB',
      100: '#FEF3C7',
      200: '#FDE68A',
      300: '#FCD34D',
      400: '#FBBF24',
      500: '#F59E0B', // Ana warning rengi
      600: '#D97706',
      700: '#B45309',
      800: '#92400E',
      900: '#78350F'  
    },
    
    // Danger Colors (Red)
    danger: {
      50: '#FEF2F2',
      100: '#FEE2E2',
      200: '#FECACA',
      300: '#FCA5A5',
      400: '#F87171',
      500: '#EF4444', // Ana danger rengi
      600: '#DC2626',
      700: '#B91C1C',
      800: '#991B1B',
      900: '#7F1D1D'
    },
    
    // Neutral Gray Colors
    gray: {
      50: '#F9FAFB',
      100: '#F3F4F6',
      200: '#E5E7EB',
      300: '#D1D5DB',
      400: '#9CA3AF',
      500: '#6B7280',
      600: '#4B5563',
      700: '#374151',
      800: '#1F2937',
      900: '#111827'
    },
    
    // Semantic Colors
    background: '#FAFAFA',
    surface: '#FFFFFF',
    onSurface: '#1F2937',
    onBackground: '#374151',
    
    // Special Colors
    accent: '#06B6D4', // Cyan accent
    info: '#3B82F6',   // Blue info
    white: '#FFFFFF',
    black: '#000000'
  },

  // Typography Scale
  typography: {
    fontFamily: {
      sans: ['Inter', 'system-ui', 'sans-serif'],
      serif: ['Merriweather', 'serif'],
      mono: ['JetBrains Mono', 'monospace']
    },
    
    fontSize: {
      xs: '0.75rem',    // 12px
      sm: '0.875rem',   // 14px
      base: '1rem',     // 16px
      lg: '1.125rem',   // 18px
      xl: '1.25rem',    // 20px
      '2xl': '1.5rem',  // 24px
      '3xl': '1.875rem', // 30px
      '4xl': '2.25rem', // 36px
      '5xl': '3rem',    // 48px
      '6xl': '3.75rem'  // 60px
    },
    
    fontWeight: {
      thin: '100',
      extralight: '200',
      light: '300',
      normal: '400',
      medium: '500',
      semibold: '600',
      bold: '700',
      extrabold: '800',
      black: '900'
    },
    
    lineHeight: {
      none: '1',
      tight: '1.25',
      snug: '1.375',
      normal: '1.5',
      relaxed: '1.625',
      loose: '2'
    }
  },

  // Spacing System (8px grid)
  spacing: {
    0: '0',
    1: '0.25rem',  // 4px
    2: '0.5rem',   // 8px
    3: '0.75rem',  // 12px
    4: '1rem',     // 16px
    5: '1.25rem',  // 20px
    6: '1.5rem',   // 24px
    8: '2rem',     // 32px
    10: '2.5rem',  // 40px
    12: '3rem',    // 48px
    16: '4rem',    // 64px
    20: '5rem',    // 80px
    24: '6rem',    // 96px
    32: '8rem',    // 128px
    40: '10rem',   // 160px
    48: '12rem',   // 192px
    56: '14rem',   // 224px
    64: '16rem'    // 256px
  },

  // Border Radius
  borderRadius: {
    none: '0',
    sm: '0.125rem',   // 2px
    base: '0.25rem',  // 4px
    md: '0.375rem',   // 6px
    lg: '0.5rem',     // 8px
    xl: '0.75rem',    // 12px
    '2xl': '1rem',    // 16px
    '3xl': '1.5rem',  // 24px
    full: '9999px'
  },

  // Shadows
  shadows: {
    sm: '0 1px 2px 0 rgba(0, 0, 0, 0.05)',
    base: '0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06)',
    md: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)',
    lg: '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)',
    xl: '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)',
    '2xl': '0 25px 50px -12px rgba(0, 0, 0, 0.25)',
    inner: 'inset 0 2px 4px 0 rgba(0, 0, 0, 0.06)'
  },

  // Animations & Transitions
  animation: {
    duration: {
      75: '75ms',
      100: '100ms',
      150: '150ms',
      200: '200ms',
      300: '300ms',
      500: '500ms',
      700: '700ms',
      1000: '1000ms'
    },
    
    easing: {
      linear: 'linear',
      in: 'cubic-bezier(0.4, 0, 1, 1)',
      out: 'cubic-bezier(0, 0, 0.2, 1)',
      inOut: 'cubic-bezier(0.4, 0, 0.2, 1)'
    }
  },

  // Breakpoints
  breakpoints: {
    sm: '640px',
    md: '768px',
    lg: '1024px',
    xl: '1280px',
    '2xl': '1536px'
  }
};

// Modern Component Styles
export const componentStyles = {
  // Glass Morphism Effect
  glassMorphism: {
    background: 'rgba(255, 255, 255, 0.25)',
    backdropFilter: 'blur(10px)',
    border: '1px solid rgba(255, 255, 255, 0.18)',
    borderRadius: modernTheme.borderRadius['2xl'],
    boxShadow: modernTheme.shadows.lg
  },

  // Gradient Backgrounds
  gradients: {
    primary: `linear-gradient(135deg, ${modernTheme.colors.primary[500]}, ${modernTheme.colors.primary[600]})`,
    success: `linear-gradient(135deg, ${modernTheme.colors.success[500]}, ${modernTheme.colors.success[600]})`,
    warning: `linear-gradient(135deg, ${modernTheme.colors.warning[500]}, ${modernTheme.colors.warning[600]})`,
    danger: `linear-gradient(135deg, ${modernTheme.colors.danger[500]}, ${modernTheme.colors.danger[600]})`,
    rainbow: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    sunset: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    ocean: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  },

  // Card Styles
  card: {
    base: {
      backgroundColor: modernTheme.colors.surface,
      borderRadius: modernTheme.borderRadius.xl,
      padding: modernTheme.spacing[6],
      boxShadow: modernTheme.shadows.md,
      border: `1px solid ${modernTheme.colors.gray[200]}`
    },
    
    elevated: {
      backgroundColor: modernTheme.colors.surface,
      borderRadius: modernTheme.borderRadius.xl,
      padding: modernTheme.spacing[6],
      boxShadow: modernTheme.shadows.xl,
      border: 'none'
    },
    
    glass: {
      background: 'rgba(255, 255, 255, 0.1)',
      backdropFilter: 'blur(10px)',
      borderRadius: modernTheme.borderRadius.xl,
      padding: modernTheme.spacing[6],
      border: '1px solid rgba(255, 255, 255, 0.2)'
    }
  },

  // Button Styles
  button: {
    base: {
      padding: `${modernTheme.spacing[3]} ${modernTheme.spacing[6]}`,
      borderRadius: modernTheme.borderRadius.lg,
      fontWeight: modernTheme.typography.fontWeight.medium,
      fontSize: modernTheme.typography.fontSize.base,
      transition: 'all 0.2s ease-in-out',
      cursor: 'pointer',
      border: 'none',
      display: 'inline-flex',
      alignItems: 'center',
      justifyContent: 'center'
    },
    
    primary: {
      background: componentStyles.gradients.primary,
      color: modernTheme.colors.white,
      boxShadow: `0 4px 14px 0 rgba(139, 92, 246, 0.4)`
    },
    
    secondary: {
      backgroundColor: modernTheme.colors.gray[100],
      color: modernTheme.colors.gray[700],
      border: `1px solid ${modernTheme.colors.gray[300]}`
    },
    
    ghost: {
      backgroundColor: 'transparent',
      color: modernTheme.colors.primary[600],
      border: `1px solid ${modernTheme.colors.primary[300]}`
    }
  },

  // Input Styles
  input: {
    base: {
      padding: modernTheme.spacing[3],
      borderRadius: modernTheme.borderRadius.lg,
      border: `2px solid ${modernTheme.colors.gray[200]}`,
      fontSize: modernTheme.typography.fontSize.base,
      backgroundColor: modernTheme.colors.surface,
      transition: 'all 0.2s ease-in-out'
    },
    
    focused: {
      borderColor: modernTheme.colors.primary[500],
      boxShadow: `0 0 0 3px rgba(139, 92, 246, 0.1)`
    },
    
    error: {
      borderColor: modernTheme.colors.danger[500],
      boxShadow: `0 0 0 3px rgba(239, 68, 68, 0.1)`
    }
  }
};

// Utility Functions
export const themeUtils = {
  // Get color with opacity
  withOpacity: (color: string, opacity: number) => {
    return `${color}${Math.round(opacity * 255).toString(16).padStart(2, '0')}`;
  },

  // Generate responsive styles
  responsive: (styles: Record<string, any>) => {
    const breakpoints = modernTheme.breakpoints;
    let responsiveStyles = '';
    
    Object.entries(styles).forEach(([breakpoint, style]) => {
      if (breakpoint === 'base') {
        responsiveStyles += Object.entries(style).map(([prop, value]) => 
          `${prop}: ${value};`
        ).join(' ');
      } else if (breakpoints[breakpoint as keyof typeof breakpoints]) {
        responsiveStyles += `
          @media (min-width: ${breakpoints[breakpoint as keyof typeof breakpoints]}) {
            ${Object.entries(style).map(([prop, value]) => 
              `${prop}: ${value};`
            ).join(' ')}
          }
        `;
      }
    });
    
    return responsiveStyles;
  },

  // Create gradient
  createGradient: (colors: string[], direction = '135deg') => {
    return `linear-gradient(${direction}, ${colors.join(', ')})`;
  },

  // Create animation
  createAnimation: (keyframes: string, duration = '300ms', easing = 'ease-in-out') => {
    return `animation: ${keyframes} ${duration} ${easing}`;
  }
};

export default modernTheme;