/**
 * WOLTAXI Mobile Application - Modern UI/UX Version
 * 
 * Ana uygulama bileşeni - Modern navigasyon, kimlik doğrulama ve 
 * gelişmiş UI/UX bileşenlerini içerir.
 * 
 * @version 3.0.0 - Modern UI/UX Update
 * @author WOLTAXI Development Team
 */

import React, { useEffect, useState } from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { StyleSheet, View, Text, Platform, PermissionsAndroid, Alert } from 'react-native';
import Icon from 'react-native-vector-icons/MaterialIcons';
import LinearGradient from 'react-native-linear-gradient';
import AsyncStorage from '@react-native-async-storage/async-storage';

// Import Modern UI Components
import WoltaxiModernUI from './components/WoltaxiModernUI';
import RideBookingUI from './components/RideBookingUI';
import DriverDashboard from './components/DriverDashboard';
import AdminDashboard from './components/AdminDashboard';

// Legacy Screens (fallback)
import SplashScreen from './screens/auth/SplashScreen';
import LoginScreen from './screens/auth/LoginScreen';
import RegisterScreen from './screens/auth/RegisterScreen';
import HomeScreen from './screens/main/HomeScreen';
import ProfileScreen from './screens/main/ProfileScreen';

// Types
interface User {
  id: number;
  phone: string;
  firstName: string;
  lastName: string;
  email?: string;
}

// Navigation
const Stack = createStackNavigator();
const Tab = createBottomTabNavigator();

// Modern Theme Colors
const theme = {
  colors: {
    primary: '#6366F1',
    secondary: '#8B5CF6',
    success: '#10B981',
    warning: '#F59E0B',
    danger: '#EF4444',
    dark: '#1F2937',
    light: '#F9FAFB',
    white: '#FFFFFF',
    gray: '#6B7280',
    background: '#F8FAFC',
    tabActive: '#6366F1',
    tabInactive: '#9CA3AF'
  }
};

/**
 * Custom Tab Bar with Modern Design
 */
const CustomTabBar = ({ state, descriptors, navigation }: any) => {
  return (
    <View style={styles.tabBarContainer}>
      <LinearGradient
        colors={[theme.colors.white, theme.colors.light]}
        style={styles.tabBarGradient}
      >
        <View style={styles.tabBar}>
          {state.routes.map((route: any, index: number) => {
            const { options } = descriptors[route.key];
            const label = options.tabBarLabel !== undefined 
              ? options.tabBarLabel 
              : options.title !== undefined 
              ? options.title 
              : route.name;

            const isFocused = state.index === index;

            const onPress = () => {
              const event = navigation.emit({
                type: 'tabPress',
                target: route.key,
                canPreventDefault: true,
              });

              if (!isFocused && !event.defaultPrevented) {
                navigation.navigate(route.name);
              }
            };

            // Tab icons mapping
            const getTabIcon = (routeName: string) => {
              switch (routeName) {
                case 'Home':
                  return 'dashboard';
                case 'RideBooking':
                  return 'local-taxi';
                case 'Driver':
                  return 'directions-car';
                case 'Admin':
                  return 'admin-panel-settings';
                default:
                  return 'home';
              }
            };

            return (
              <View key={index} style={styles.tabItem}>
                {isFocused && (
                  <LinearGradient
                    colors={[theme.colors.primary, theme.colors.secondary]}
                    style={styles.activeTabBackground}
                  />
                )}
                <View 
                  style={[
                    styles.tabButton,
                    isFocused && styles.activeTabButton
                  ]}
                >
                  <Icon
                    name={getTabIcon(route.name)}
                    size={24}
                    color={isFocused ? theme.colors.white : theme.colors.tabInactive}
                    onPress={onPress}
                  />
                  <Text style={[
                    styles.tabLabel,
                    { color: isFocused ? theme.colors.white : theme.colors.tabInactive }
                  ]}>
                    {label}
                  </Text>
                </View>
              </View>
            );
          })}
        </View>
      </LinearGradient>
    </View>
  );
};

/**
 * Modern Main Tab Navigator
 */
const MainTabs: React.FC = () => {
  return (
    <Tab.Navigator
      tabBar={(props) => <CustomTabBar {...props} />}
      screenOptions={{
        headerShown: false,
      }}
    >
      <Tab.Screen 
        name="Home" 
        component={WoltaxiModernUI}
        options={{
          tabBarLabel: 'Ana Sayfa',
        }}
      />
      <Tab.Screen 
        name="RideBooking" 
        component={RideBookingUI}
        options={{
          tabBarLabel: 'Taksi Çağır',
        }}
      />
      <Tab.Screen 
        name="Driver" 
        component={DriverDashboard}
        options={{
          tabBarLabel: 'Sürücü',
        }}
      />
      <Tab.Screen 
        name="Admin" 
        component={AdminDashboard}
        options={{
          tabBarLabel: 'Yönetim',
        }}
      />
    </Tab.Navigator>
  );
};

/**
 * Kimlik Doğrulama Navigasyonu
 */
const AuthStack: React.FC = () => {
  return (
    <Stack.Navigator
      initialRouteName="Login"
      screenOptions={{
        headerShown: false,
        cardStyle: { backgroundColor: 'white' },
      }}
    >
      <Stack.Screen name="Login" component={LoginScreen} />
      <Stack.Screen name="Register" component={RegisterScreen} />
    </Stack.Navigator>
  );
};

/**
 * Ana Uygulama Bileşeni
 */
const App: React.FC = () => {
  const [isLoading, setIsLoading] = useState(true);
  const [user, setUser] = useState<User | null>(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  useEffect(() => {
    initializeApp();
  }, []);

  /**
   * Uygulama Başlatma
   */
  const initializeApp = async () => {
    try {
      await requestPermissions();
      configurePushNotifications();
      await checkAuthStatus();
    } catch (error) {
      console.error('App initialization error:', error);
    } finally {
      setIsLoading(false);
    }
  };

  /**
   * İzinleri İste
   */
  const requestPermissions = async () => {
    if (Platform.OS === 'android') {
      try {
        const grants = await PermissionsAndroid.requestMultiple([
          PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
          PermissionsAndroid.PERMISSIONS.ACCESS_COARSE_LOCATION,
        ]);
        
        const locationGranted = grants['android.permission.ACCESS_FINE_LOCATION'] === 'granted' ||
                               grants['android.permission.ACCESS_COARSE_LOCATION'] === 'granted';
                               
        if (!locationGranted) {
          Alert.alert(
            'Konum İzni Gerekli', 
            'WOLTAXI uygulamasının düzgün çalışması için konum izni gereklidir.'
          );
        }
      } catch (error) {
        console.error('Permission request error:', error);
      }
    }
  };

  /**
   * Push Notification Yapılandırması
   */
  const configurePushNotifications = () => {
    PushNotification.configure({
      onRegister: function (token) {
        console.log('FCM Token:', token);
      },
      
      onNotification: function (notification) {
        console.log('Notification received:', notification);
      },
      
      permissions: {
        alert: true,
        badge: true,
        sound: true,
      },
      
      popInitialNotification: true,
      requestPermissions: Platform.OS === 'ios',
    });
  };

  /**
   * Kimlik Doğrulama Kontrolü
   */
  const checkAuthStatus = async () => {
    try {
      const token = await AsyncStorage.getItem('authToken');
      const userData = await AsyncStorage.getItem('userData');
      
      if (token && userData) {
        const parsedUser = JSON.parse(userData);
        setUser(parsedUser);
        setIsAuthenticated(true);
      }
    } catch (error) {
      console.error('Auth check error:', error);
    }
  };

  // Yükleme ekranı
  if (isLoading) {
    return <SplashScreen />;
  }

  return (
    <NavigationContainer>
      <View style={styles.container}>
        <Stack.Navigator
          screenOptions={{
            headerShown: false,
            cardStyle: { backgroundColor: theme.colors.background },
          }}
        >
          {isAuthenticated ? (
            <Stack.Screen name="MainTabs" component={MainTabs} />
          ) : (
            <Stack.Screen name="Auth" component={AuthStack} />
          )}
        </Stack.Navigator>
      </View>
    </NavigationContainer>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: theme.colors.background,
  },

  // Custom Tab Bar Styles
  tabBarContainer: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    elevation: 20,
    shadowColor: theme.colors.dark,
    shadowOffset: { width: 0, height: -4 },
    shadowOpacity: 0.1,
    shadowRadius: 8,
  },
  tabBarGradient: {
    borderTopLeftRadius: 25,
    borderTopRightRadius: 25,
    paddingBottom: Platform.OS === 'ios' ? 25 : 15, // Safe area for home indicator
  },
  tabBar: {
    flexDirection: 'row',
    paddingTop: 15,
    paddingHorizontal: 20,
    borderTopLeftRadius: 25,
    borderTopRightRadius: 25,
  },
  tabItem: {
    flex: 1,
    alignItems: 'center',
    position: 'relative',
  },
  activeTabBackground: {
    position: 'absolute',
    top: -5,
    left: '20%',
    right: '20%',
    height: 50,
    borderRadius: 25,
    zIndex: 0,
  },
  tabButton: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 8,
    paddingHorizontal: 12,
    borderRadius: 20,
    minHeight: 45,
    zIndex: 1,
  },
  activeTabButton: {
    // Active state styling handled by gradient background
  },
  tabLabel: {
    fontSize: 11,
    fontWeight: '600',
    marginTop: 4,
    textAlign: 'center',
  },
});

export default App;
