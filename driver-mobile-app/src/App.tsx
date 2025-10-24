/**
 * WOLTAXI Driver Mobile Application
 * 
 * Sürücüler için özel mobil uygulama
 * - Abonelik paketleri yönetimi
 * - Müşteri portföyü ve CRM
 * - Kazanç takibi ve raporlar
 * - Yolculuk yönetimi
 * - Performans analitiği
 * 
 * @version 2.0.0
 * @author WOLTAXI Development Team
 */

import React, { useEffect, useState } from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { createDrawerNavigator } from '@react-navigation/drawer';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { StatusBar, Platform, Alert } from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import Icon from 'react-native-vector-icons/MaterialIcons';
import { Provider as PaperProvider } from 'react-native-paper';

// Screens - Auth
import SplashScreen from './src/screens/auth/SplashScreen';
import LoginScreen from './src/screens/auth/LoginScreen';
import RegisterScreen from './src/screens/auth/RegisterScreen';

// Screens - Main
import DashboardScreen from './src/screens/main/DashboardScreen';
import RidesScreen from './src/screens/main/RidesScreen';
import EarningsScreen from './src/screens/main/EarningsScreen';
import CustomerPortfolioScreen from './src/screens/main/CustomerPortfolioScreen';
import ProfileScreen from './src/screens/main/ProfileScreen';

// Screens - Subscription
import SubscriptionScreen from './src/screens/subscription/SubscriptionScreen';
import PackagesScreen from './src/screens/subscription/PackagesScreen';
import PaymentScreen from './src/screens/subscription/PaymentScreen';

// Screens - Portfolio
import PortfolioListScreen from './src/screens/portfolio/PortfolioListScreen';
import CustomerDetailScreen from './src/screens/portfolio/CustomerDetailScreen';
import AddCustomerScreen from './src/screens/portfolio/AddCustomerScreen';

// Theme
import { driverAppTheme } from './src/theme/theme';

// Types
interface Driver {
  id: number;
  firstName: string;
  lastName: string;
  phone: string;
  currentSubscription?: any;
}

// Navigation
const Stack = createStackNavigator();
const Tab = createBottomTabNavigator();
const Drawer = createDrawerNavigator();

/**
 * Ana Tab Navigasyonu - Sürücü App
 */
const MainTabs: React.FC = () => {
  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        tabBarIcon: ({ focused, color, size }) => {
          let iconName: string;

          switch (route.name) {
            case 'Dashboard':
              iconName = 'dashboard';
              break;
            case 'Rides':
              iconName = 'local-taxi';
              break;
            case 'Portfolio':
              iconName = 'people';
              break;
            case 'Earnings':
              iconName = 'account-balance-wallet';
              break;
            case 'Profile':
              iconName = 'person';
              break;
            default:
              iconName = 'circle';
          }

          return <Icon name={iconName} size={size} color={color} />;
        },
        tabBarActiveTintColor: '#E30613',
        tabBarInactiveTintColor: 'gray',
        tabBarStyle: {
          backgroundColor: 'white',
          borderTopWidth: 1,
          borderTopColor: '#E0E0E0',
          height: Platform.OS === 'ios' ? 85 : 65,
          paddingBottom: Platform.OS === 'ios' ? 25 : 10,
        },
        headerShown: false,
      })}
    >
      <Tab.Screen 
        name="Dashboard" 
        component={DashboardScreen}
        options={{ tabBarLabel: 'Ana Sayfa' }}
      />
      <Tab.Screen 
        name="Rides" 
        component={RidesScreen}
        options={{ tabBarLabel: 'Yolculuklar' }}
      />
      <Tab.Screen 
        name="Portfolio" 
        component={CustomerPortfolioScreen}
        options={{ tabBarLabel: 'Müşteriler' }}
      />
      <Tab.Screen 
        name="Earnings" 
        component={EarningsScreen}
        options={{ tabBarLabel: 'Kazançlar' }}
      />
      <Tab.Screen 
        name="Profile" 
        component={ProfileScreen}
        options={{ tabBarLabel: 'Profil' }}
      />
    </Tab.Navigator>
  );
};

/**
 * Ana Drawer Navigasyonu
 */
const MainDrawer: React.FC = () => {
  return (
    <Drawer.Navigator
      screenOptions={{
        headerShown: true,
        drawerStyle: {
          backgroundColor: '#f8f9fa',
          width: 280,
        },
        drawerActiveTintColor: '#E30613',
        drawerInactiveTintColor: '#666',
      }}
    >
      <Drawer.Screen 
        name="MainTabs" 
        component={MainTabs}
        options={{
          title: 'WOLTAXI Sürücü',
          drawerLabel: 'Ana Sayfa',
          drawerIcon: ({ color, size }) => (
            <Icon name="home" size={size} color={color} />
          ),
        }}
      />
      <Drawer.Screen 
        name="Subscription" 
        component={SubscriptionScreen}
        options={{
          title: 'Abonelik Paketim',
          drawerLabel: 'Abonelik',
          drawerIcon: ({ color, size }) => (
            <Icon name="card-membership" size={size} color={color} />
          ),
        }}
      />
      <Drawer.Screen 
        name="Packages" 
        component={PackagesScreen}
        options={{
          title: 'Paket Seçenekleri',
          drawerLabel: 'Paketler',
          drawerIcon: ({ color, size }) => (
            <Icon name="shopping-cart" size={size} color={color} />
          ),
        }}
      />
    </Drawer.Navigator>
  );
};

/**
 * Auth Stack Navigasyonu
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
 * Ana Driver App Bileşeni
 */
const DriverApp: React.FC = () => {
  const [isLoading, setIsLoading] = useState(true);
  const [driver, setDriver] = useState<Driver | null>(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  useEffect(() => {
    initializeApp();
  }, []);

  /**
   * Uygulama Başlatma
   */
  const initializeApp = async () => {
    try {
      await checkAuthStatus();
      // Diğer başlatma işlemleri
    } catch (error) {
      console.error('Driver App initialization error:', error);
    } finally {
      setIsLoading(false);
    }
  };

  /**
   * Kimlik Doğrulama Kontrolü
   */
  const checkAuthStatus = async () => {
    try {
      const token = await AsyncStorage.getItem('driverAuthToken');
      const driverData = await AsyncStorage.getItem('driverData');
      
      if (token && driverData) {
        const parsedDriver = JSON.parse(driverData);
        setDriver(parsedDriver);
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
    <PaperProvider theme={driverAppTheme}>
      <SafeAreaProvider>
        <StatusBar 
          barStyle="dark-content" 
          backgroundColor="white" 
          translucent={false}
        />
        <NavigationContainer>
          <Stack.Navigator
            screenOptions={{
              headerShown: false,
              cardStyle: { backgroundColor: 'white' },
            }}
          >
            {isAuthenticated ? (
              <>
                <Stack.Screen name="MainDrawer" component={MainDrawer} />
                
                {/* Modal Screens */}
                <Stack.Screen 
                  name="Payment" 
                  component={PaymentScreen}
                  options={{
                    presentation: 'modal',
                    headerShown: true,
                    title: 'Ödeme',
                  }}
                />
                <Stack.Screen 
                  name="CustomerDetail" 
                  component={CustomerDetailScreen}
                  options={{
                    presentation: 'modal',
                    headerShown: true,
                    title: 'Müşteri Detayı',
                  }}
                />
                <Stack.Screen 
                  name="AddCustomer" 
                  component={AddCustomerScreen}
                  options={{
                    presentation: 'modal',
                    headerShown: true,
                    title: 'Müşteri Ekle',
                  }}
                />
              </>
            ) : (
              <Stack.Screen name="Auth" component={AuthStack} />
            )}
          </Stack.Navigator>
        </NavigationContainer>
      </SafeAreaProvider>
    </PaperProvider>
  );
};

export default DriverApp;