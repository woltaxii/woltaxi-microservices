import React from 'react';
import { 
  StyleSheet, 
  View, 
  Text, 
  TouchableOpacity, 
  ScrollView,
  Dimensions,
  StatusBar,
  SafeAreaView
} from 'react-native';
import LinearGradient from 'react-native-linear-gradient';
import { BlurView } from '@react-native-blur/blur';
import Icon from 'react-native-vector-icons/MaterialIcons';
import Animated, { 
  useSharedValue, 
  useAnimatedStyle, 
  withSpring,
  withTiming,
  interpolate 
} from 'react-native-reanimated';

const { width, height } = Dimensions.get('window');

// Modern Color Palette
const colors = {
  primary: '#6C5CE7', // Purple
  secondary: '#A29BFE', // Light Purple  
  accent: '#00B894', // Emerald
  success: '#00B894',
  warning: '#FDCB6E',
  danger: '#E17055',
  dark: '#2D3436',
  light: '#DDD6FE',
  white: '#FFFFFF',
  gray: '#636E72',
  lightGray: '#B2BEC3',
  background: '#F8F9FA',
  surface: '#FFFFFF',
  overlay: 'rgba(108, 92, 231, 0.9)'
};

// Gradient Combinations
const gradients = {
  primary: ['#6C5CE7', '#A29BFE'],
  success: ['#00B894', '#55EFC4'],
  warning: ['#FDCB6E', '#F39C12'],
  danger: ['#E17055', '#E84393'],
  dark: ['#2D3436', '#636E72'],
  glass: ['rgba(255,255,255,0.25)', 'rgba(255,255,255,0.1)']
};

/**
 * Modern Glass Morphism Card Component
 */
const GlassMorphCard: React.FC<{
  children: React.ReactNode;
  style?: any;
  gradient?: string[];
}> = ({ children, style, gradient = gradients.glass }) => {
  return (
    <LinearGradient
      colors={gradient}
      style={[styles.glassMorphCard, style]}
      start={{ x: 0, y: 0 }}
      end={{ x: 1, y: 1 }}
    >
      <BlurView
        style={styles.blurContainer}
        blurType="light"
        blurAmount={10}
        reducedTransparencyFallbackColor={colors.white}
      >
        {children}
      </BlurView>
    </LinearGradient>
  );
};

/**
 * Animated Floating Action Button
 */
const FloatingActionButton: React.FC<{
  onPress: () => void;
  icon: string;
  color?: string;
}> = ({ onPress, icon, color = colors.primary }) => {
  const scale = useSharedValue(1);
  const rotation = useSharedValue(0);

  const animatedStyle = useAnimatedStyle(() => {
    return {
      transform: [
        { scale: scale.value },
        { rotate: `${rotation.value}deg` }
      ],
    };
  });

  const handlePress = () => {
    scale.value = withSpring(0.9, { duration: 100 }, () => {
      scale.value = withSpring(1);
    });
    rotation.value = withTiming(rotation.value + 180, { duration: 300 });
    onPress();
  };

  return (
    <Animated.View style={[styles.fab, animatedStyle]}>
      <TouchableOpacity onPress={handlePress} style={styles.fabButton}>
        <LinearGradient
          colors={[color, `${color}CC`]}
          style={styles.fabGradient}
          start={{ x: 0, y: 0 }}
          end={{ x: 1, y: 1 }}
        >
          <Icon name={icon} size={24} color={colors.white} />
        </LinearGradient>
      </TouchableOpacity>
    </Animated.View>
  );
};

/**
 * Modern Service Card with Micro-interactions
 */
const ServiceCard: React.FC<{
  title: string;
  subtitle: string;
  icon: string;
  color: string;
  onPress: () => void;
}> = ({ title, subtitle, icon, color, onPress }) => {
  const scale = useSharedValue(1);
  const opacity = useSharedValue(1);

  const animatedStyle = useAnimatedStyle(() => {
    return {
      transform: [{ scale: scale.value }],
      opacity: opacity.value,
    };
  });

  const handlePressIn = () => {
    scale.value = withSpring(0.95);
    opacity.value = withTiming(0.8);
  };

  const handlePressOut = () => {
    scale.value = withSpring(1);
    opacity.value = withTiming(1);
  };

  return (
    <Animated.View style={animatedStyle}>
      <TouchableOpacity
        onPressIn={handlePressIn}
        onPressOut={handlePressOut}
        onPress={onPress}
        activeOpacity={1}
      >
        <GlassMorphCard style={styles.serviceCard}>
          <View style={styles.serviceCardContent}>
            <View style={[styles.serviceIcon, { backgroundColor: color }]}>
              <Icon name={icon} size={28} color={colors.white} />
            </View>
            <View style={styles.serviceTextContainer}>
              <Text style={styles.serviceTitle}>{title}</Text>
              <Text style={styles.serviceSubtitle}>{subtitle}</Text>
            </View>
            <Icon name="arrow-forward-ios" size={16} color={colors.gray} />
          </View>
        </GlassMorphCard>
      </TouchableOpacity>
    </Animated.View>
  );
};

/**
 * Modern Stats Widget
 */
const StatsWidget: React.FC<{
  title: string;
  value: string;
  change: string;
  trend: 'up' | 'down';
  color: string;
}> = ({ title, value, change, trend, color }) => {
  return (
    <GlassMorphCard style={styles.statsWidget}>
      <View style={styles.statsContent}>
        <Text style={styles.statsTitle}>{title}</Text>
        <Text style={[styles.statsValue, { color }]}>{value}</Text>
        <View style={styles.statsChange}>
          <Icon 
            name={trend === 'up' ? 'trending-up' : 'trending-down'} 
            size={16} 
            color={trend === 'up' ? colors.success : colors.danger} 
          />
          <Text style={[
            styles.statsChangeText,
            { color: trend === 'up' ? colors.success : colors.danger }
          ]}>
            {change}
          </Text>
        </View>
      </View>
    </GlassMorphCard>
  );
};

/**
 * Main WOLTAXI Modern UI Component
 */
const WoltaxiModernUI: React.FC = () => {
  const headerOpacity = useSharedValue(1);

  const services = [
    { title: 'Ride Booking', subtitle: 'Book your taxi instantly', icon: 'local-taxi', color: colors.primary },
    { title: 'Delivery Service', subtitle: 'Fast package delivery', icon: 'delivery-dining', color: colors.accent },
    { title: 'Travel Planning', subtitle: 'Plan your journeys', icon: 'flight', color: colors.warning },
    { title: 'Emergency Help', subtitle: '24/7 emergency support', icon: 'emergency', color: colors.danger },
  ];

  const stats = [
    { title: 'Active Rides', value: '1,234', change: '+12%', trend: 'up' as const, color: colors.primary },
    { title: 'Revenue', value: '₺45.2K', change: '+8.5%', trend: 'up' as const, color: colors.success },
    { title: 'Drivers', value: '892', change: '-2.1%', trend: 'down' as const, color: colors.warning },
  ];

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar backgroundColor={colors.primary} barStyle="light-content" />
      
      {/* Header with Gradient */}
      <LinearGradient
        colors={gradients.primary}
        style={styles.header}
        start={{ x: 0, y: 0 }}
        end={{ x: 1, y: 1 }}
      >
        <View style={styles.headerContent}>
          <View>
            <Text style={styles.headerGreeting}>Merhaba! 👋</Text>
            <Text style={styles.headerTitle}>WOLTAXI Dashboard</Text>
          </View>
          <TouchableOpacity style={styles.profileButton}>
            <Icon name="account-circle" size={40} color={colors.white} />
          </TouchableOpacity>
        </View>
      </LinearGradient>

      <ScrollView style={styles.content} showsVerticalScrollIndicator={false}>
        
        {/* Stats Section */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>📊 Analytics Overview</Text>
          <ScrollView horizontal showsHorizontalScrollIndicator={false}>
            <View style={styles.statsContainer}>
              {stats.map((stat, index) => (
                <StatsWidget key={index} {...stat} />
              ))}
            </View>
          </ScrollView>
        </View>

        {/* Services Section */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>🚀 Services</Text>
          {services.map((service, index) => (
            <ServiceCard
              key={index}
              {...service}
              onPress={() => console.log(`${service.title} pressed`)}
            />
          ))}
        </View>

        {/* Modern Features Section */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>✨ AI Features</Text>
          
          <GlassMorphCard style={styles.featureCard}>
            <LinearGradient
              colors={['#667eea', '#764ba2']}
              style={styles.featureGradient}
              start={{ x: 0, y: 0 }}
              end={{ x: 1, y: 1 }}
            >
              <View style={styles.featureContent}>
                <Icon name="smart-toy" size={32} color={colors.white} />
                <View style={styles.featureText}>
                  <Text style={styles.featureTitle}>Autonomous Driving</Text>
                  <Text style={styles.featureSubtitle}>AI-powered self-driving technology</Text>
                </View>
              </View>
            </LinearGradient>
          </GlassMorphCard>

          <GlassMorphCard style={styles.featureCard}>
            <LinearGradient
              colors={['#f093fb', '#f5576c']}
              style={styles.featureGradient}
              start={{ x: 0, y: 0 }}
              end={{ x: 1, y: 1 }}
            >
              <View style={styles.featureContent}>
                <Icon name="analytics" size={32} color={colors.white} />
                <View style={styles.featureText}>
                  <Text style={styles.featureTitle}>Smart Analytics</Text>
                  <Text style={styles.featureSubtitle}>Real-time business intelligence</Text>
                </View>
              </View>
            </LinearGradient>
          </GlassMorphCard>

        </View>

        <View style={styles.bottomSpacer} />
      </ScrollView>

      {/* Floating Action Button */}
      <FloatingActionButton
        onPress={() => console.log('FAB pressed')}
        icon="add"
        color={colors.accent}
      />
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  
  // Header Styles
  header: {
    paddingTop: 20,
    paddingBottom: 30,
    paddingHorizontal: 20,
    borderBottomLeftRadius: 30,
    borderBottomRightRadius: 30,
    elevation: 10,
    shadowColor: colors.primary,
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 10,
  },
  headerContent: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: 10,
  },
  headerGreeting: {
    color: colors.white,
    fontSize: 16,
    opacity: 0.9,
    fontWeight: '400',
  },
  headerTitle: {
    color: colors.white,
    fontSize: 28,
    fontWeight: 'bold',
    marginTop: 4,
  },
  profileButton: {
    borderRadius: 20,
    backgroundColor: 'rgba(255,255,255,0.2)',
    padding: 2,
  },

  // Content Styles
  content: {
    flex: 1,
    paddingHorizontal: 20,
  },
  section: {
    marginTop: 25,
  },
  sectionTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: colors.dark,
    marginBottom: 15,
  },

  // Glass Morphism Card
  glassMorphCard: {
    borderRadius: 20,
    marginBottom: 15,
    elevation: 5,
    shadowColor: colors.dark,
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 10,
  },
  blurContainer: {
    borderRadius: 20,
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.2)',
    overflow: 'hidden',
  },

  // Service Card Styles
  serviceCard: {
    marginBottom: 12,
  },
  serviceCardContent: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 20,
  },
  serviceIcon: {
    width: 50,
    height: 50,
    borderRadius: 15,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 15,
  },
  serviceTextContainer: {
    flex: 1,
  },
  serviceTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    color: colors.dark,
    marginBottom: 4,
  },
  serviceSubtitle: {
    fontSize: 14,
    color: colors.gray,
  },

  // Stats Widget Styles
  statsContainer: {
    flexDirection: 'row',
    paddingRight: 20,
  },
  statsWidget: {
    width: 140,
    marginRight: 15,
  },
  statsContent: {
    padding: 18,
    alignItems: 'center',
  },
  statsTitle: {
    fontSize: 12,
    color: colors.gray,
    textAlign: 'center',
    marginBottom: 8,
  },
  statsValue: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 8,
  },
  statsChange: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  statsChangeText: {
    fontSize: 12,
    fontWeight: '600',
    marginLeft: 4,
  },

  // Feature Card Styles
  featureCard: {
    marginBottom: 15,
    height: 100,
  },
  featureGradient: {
    flex: 1,
    borderRadius: 20,
    padding: 20,
    justifyContent: 'center',
  },
  featureContent: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  featureText: {
    marginLeft: 15,
    flex: 1,
  },
  featureTitle: {
    color: colors.white,
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 4,
  },
  featureSubtitle: {
    color: colors.white,
    fontSize: 14,
    opacity: 0.9,
  },

  // Floating Action Button
  fab: {
    position: 'absolute',
    right: 20,
    bottom: 30,
  },
  fabButton: {
    width: 56,
    height: 56,
    borderRadius: 28,
    elevation: 8,
    shadowColor: colors.dark,
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
  },
  fabGradient: {
    width: 56,
    height: 56,
    borderRadius: 28,
    justifyContent: 'center',
    alignItems: 'center',
  },

  bottomSpacer: {
    height: 100,
  },
});

export default WoltaxiModernUI;