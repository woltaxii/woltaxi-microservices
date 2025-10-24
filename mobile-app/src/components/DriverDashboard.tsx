import React, { useState, useEffect } from 'react';
import {
  StyleSheet,
  View,
  Text,
  TouchableOpacity,
  ScrollView,
  Dimensions,
  StatusBar,
  Switch,
  Alert
} from 'react-native';
import LinearGradient from 'react-native-linear-gradient';
import Icon from 'react-native-vector-icons/MaterialIcons';
import MapView, { Marker, PROVIDER_GOOGLE } from 'react-native-maps';
import { BlurView } from '@react-native-blur/blur';
import Animated, { 
  useSharedValue, 
  useAnimatedStyle, 
  withSpring,
  withTiming,
  interpolate 
} from 'react-native-reanimated';

const { width, height } = Dimensions.get('window');

// Modern Driver Dashboard Theme
const theme = {
  colors: {
    primary: '#3B82F6',
    secondary: '#8B5CF6',
    success: '#10B981',
    warning: '#F59E0B',
    danger: '#EF4444',
    dark: '#1F2937',
    light: '#F9FAFB',
    white: '#FFFFFF',
    gray: '#6B7280',
    background: '#F1F5F9'
  },
  spacing: {
    xs: 4,
    sm: 8,
    md: 16,
    lg: 24,
    xl: 32
  },
  borderRadius: {
    sm: 8,
    md: 12,
    lg: 16,
    xl: 20,
    full: 999
  }
};

/**
 * Modern Driver Dashboard Interface
 */
const DriverDashboard: React.FC = () => {
  const [isOnline, setIsOnline] = useState(false);
  const [currentRide, setCurrentRide] = useState(null);
  const [earnings, setEarnings] = useState(0);
  const [totalRides, setTotalRides] = useState(0);
  const [rating, setRating] = useState(4.8);

  const pulseAnim = useSharedValue(1);
  const onlineStatusAnim = useSharedValue(0);

  // Animation for online status
  useEffect(() => {
    if (isOnline) {
      onlineStatusAnim.value = withSpring(1);
      // Pulse animation for online indicator
      const pulse = () => {
        pulseAnim.value = withTiming(1.2, { duration: 1000 }, () => {
          pulseAnim.value = withTiming(1, { duration: 1000 }, pulse);
        });
      };
      pulse();
    } else {
      onlineStatusAnim.value = withSpring(0);
      pulseAnim.value = withTiming(1);
    }
  }, [isOnline]);

  const pulseStyle = useAnimatedStyle(() => {
    return {
      transform: [{ scale: pulseAnim.value }],
    };
  });

  const statusStyle = useAnimatedStyle(() => {
    return {
      opacity: onlineStatusAnim.value,
      transform: [{ scale: onlineStatusAnim.value }],
    };
  });

  // Sample ride requests
  const rideRequests = [
    {
      id: 1,
      pickup: 'Taksim Meydanı',
      destination: 'Kadıköy İskele',
      distance: '8.5 km',
      estimatedEarning: '₺45',
      passengerName: 'Ahmet K.',
      rating: 4.9,
      waitTime: '2 dk'
    },
    {
      id: 2,
      pickup: 'Beşiktaş Çarşı',
      destination: 'Levent Metro',
      distance: '4.2 km',
      estimatedEarning: '₺28',
      passengerName: 'Zeynep A.',
      rating: 4.7,
      waitTime: '5 dk'
    }
  ];

  const handleToggleOnline = () => {
    setIsOnline(!isOnline);
    if (!isOnline) {
      Alert.alert('Çevrimiçi Oldunuz! 🚗', 'Yolcu istekleri alabilirsiniz.');
    } else {
      Alert.alert('Çevrimdışı Oldunuz', 'Yeni yolcu istekleri almayacaksınız.');
    }
  };

  const handleAcceptRide = (rideId: number) => {
    Alert.alert('Yolculuk Kabul Edildi! ✅', 'Yolcuya yönlendiriliyorsunuz.');
    setCurrentRide(rideId);
  };

  const renderStatusCard = () => (
    <View style={styles.statusCard}>
      <LinearGradient
        colors={isOnline ? [theme.colors.success, '#34D399'] : [theme.colors.gray, '#9CA3AF']}
        style={styles.statusGradient}
      >
        <View style={styles.statusContent}>
          <View style={styles.statusLeft}>
            <Animated.View style={[styles.onlineIndicator, pulseStyle]}>
              <View style={[styles.onlineIndicatorInner, { backgroundColor: isOnline ? theme.colors.white : theme.colors.gray }]} />
            </Animated.View>
            <View>
              <Text style={styles.statusTitle}>
                {isOnline ? 'ÇEVRİMİÇİ' : 'ÇEVRİMDIŞI'}
              </Text>
              <Text style={styles.statusSubtitle}>
                {isOnline ? 'Yolcu istekleri alınıyor' : 'Çevrimiçi olmak için değiştirin'}
              </Text>
            </View>
          </View>
          
          <Switch
            value={isOnline}
            onValueChange={handleToggleOnline}
            trackColor={{ false: '#767577', true: '#34D399' }}
            thumbColor={theme.colors.white}
            ios_backgroundColor="#3e3e3e"
          />
        </View>
      </LinearGradient>
    </View>
  );

  const renderEarningsStats = () => (
    <View style={styles.statsContainer}>
      <View style={styles.statCard}>
        <LinearGradient
          colors={[theme.colors.primary, '#60A5FA']}
          style={styles.statGradient}
        >
          <Icon name="attach-money" size={32} color={theme.colors.white} />
          <Text style={styles.statValue}>₺{earnings.toFixed(2)}</Text>
          <Text style={styles.statLabel}>Bugünkü Kazanç</Text>
        </LinearGradient>
      </View>
      
      <View style={styles.statCard}>
        <LinearGradient
          colors={[theme.colors.warning, '#FBBF24']}
          style={styles.statGradient}
        >
          <Icon name="local-taxi" size={32} color={theme.colors.white} />
          <Text style={styles.statValue}>{totalRides}</Text>
          <Text style={styles.statLabel}>Toplam Yolculuk</Text>
        </LinearGradient>
      </View>
      
      <View style={styles.statCard}>
        <LinearGradient
          colors={[theme.colors.secondary, '#A78BFA']}
          style={styles.statGradient}
        >
          <Icon name="star" size={32} color={theme.colors.white} />
          <Text style={styles.statValue}>{rating}</Text>
          <Text style={styles.statLabel}>Değerlendirme</Text>
        </LinearGradient>
      </View>
    </View>
  );

  const renderRideRequests = () => (
    <View style={styles.requestsContainer}>
      <Text style={styles.sectionTitle}>📱 Yolcu İstekleri</Text>
      
      {!isOnline ? (
        <View style={styles.offlineMessage}>
          <Icon name="power-settings-new" size={48} color={theme.colors.gray} />
          <Text style={styles.offlineText}>Çevrimiçi olun</Text>
          <Text style={styles.offlineSubtext}>Yolcu isteklerini görmek için çevrimiçi olmalısınız</Text>
        </View>
      ) : (
        <ScrollView showsVerticalScrollIndicator={false}>
          {rideRequests.map((request) => (
            <View key={request.id} style={styles.requestCard}>
              <BlurView style={styles.requestBlur} blurType="light" blurAmount={10}>
                <View style={styles.requestHeader}>
                  <View style={styles.passengerInfo}>
                    <Icon name="account-circle" size={40} color={theme.colors.primary} />
                    <View style={styles.passengerDetails}>
                      <Text style={styles.passengerName}>{request.passengerName}</Text>
                      <View style={styles.ratingContainer}>
                        <Icon name="star" size={16} color={theme.colors.warning} />
                        <Text style={styles.passengerRating}>{request.rating}</Text>
                      </View>
                    </View>
                  </View>
                  <View style={styles.waitTime}>
                    <Text style={styles.waitTimeText}>{request.waitTime}</Text>
                  </View>
                </View>

                <View style={styles.routeInfo}>
                  <View style={styles.routeDots}>
                    <View style={[styles.dot, { backgroundColor: theme.colors.success }]} />
                    <View style={styles.connectionLine} />
                    <View style={[styles.dot, { backgroundColor: theme.colors.danger }]} />
                  </View>
                  
                  <View style={styles.routeTexts}>
                    <Text style={styles.routeText}>
                      <Icon name="my-location" size={14} color={theme.colors.success} /> {request.pickup}
                    </Text>
                    <Text style={styles.routeText}>
                      <Icon name="place" size={14} color={theme.colors.danger} /> {request.destination}
                    </Text>
                  </View>
                </View>

                <View style={styles.requestFooter}>
                  <View style={styles.requestDetails}>
                    <Text style={styles.distanceText}>{request.distance}</Text>
                    <Text style={styles.earningText}>{request.estimatedEarning}</Text>
                  </View>
                  
                  <View style={styles.requestActions}>
                    <TouchableOpacity style={styles.declineButton}>
                      <Icon name="close" size={20} color={theme.colors.danger} />
                    </TouchableOpacity>
                    
                    <TouchableOpacity 
                      style={styles.acceptButton}
                      onPress={() => handleAcceptRide(request.id)}
                    >
                      <LinearGradient
                        colors={[theme.colors.success, '#34D399']}
                        style={styles.acceptButtonGradient}
                      >
                        <Icon name="check" size={20} color={theme.colors.white} />
                        <Text style={styles.acceptButtonText}>Kabul Et</Text>
                      </LinearGradient>
                    </TouchableOpacity>
                  </View>
                </View>
              </BlurView>
            </View>
          ))}
        </ScrollView>
      )}
    </View>
  );

  const renderDriverMap = () => (
    <View style={styles.mapContainer}>
      <Text style={styles.sectionTitle}>🗺️ Konum</Text>
      <View style={styles.mapWrapper}>
        <MapView
          provider={PROVIDER_GOOGLE}
          style={styles.map}
          initialRegion={{
            latitude: 41.0082,
            longitude: 28.9784,
            latitudeDelta: 0.01,
            longitudeDelta: 0.01,
          }}
        >
          <Marker
            coordinate={{ latitude: 41.0082, longitude: 28.9784 }}
            title="Sizin Konumunuz"
          >
            <View style={styles.driverMarker}>
              <Icon name="local-taxi" size={24} color={theme.colors.white} />
            </View>
          </Marker>
        </MapView>
        
        {isOnline && (
          <Animated.View style={[styles.onlineOverlay, statusStyle]}>
            <BlurView style={styles.onlineInfo} blurType="light" blurAmount={10}>
              <Text style={styles.onlineInfoText}>🟢 Çevrimiçi</Text>
            </BlurView>
          </Animated.View>
        )}
      </View>
    </View>
  );

  return (
    <View style={styles.container}>
      <StatusBar backgroundColor={theme.colors.primary} barStyle="light-content" />
      
      {/* Header */}
      <LinearGradient
        colors={[theme.colors.primary, theme.colors.secondary]}
        style={styles.header}
      >
        <View style={styles.headerContent}>
          <View>
            <Text style={styles.headerGreeting}>Merhaba Sürücü! 👋</Text>
            <Text style={styles.headerTitle}>WOLTAXI Şoför</Text>
          </View>
          <TouchableOpacity style={styles.profileButton}>
            <Icon name="account-circle" size={40} color={theme.colors.white} />
          </TouchableOpacity>
        </View>
      </LinearGradient>

      <ScrollView style={styles.content} showsVerticalScrollIndicator={false}>
        {renderStatusCard()}
        {renderEarningsStats()}
        {renderDriverMap()}
        {renderRideRequests()}
        
        <View style={styles.bottomSpacer} />
      </ScrollView>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: theme.colors.background,
  },
  
  // Header Styles
  header: {
    paddingTop: 50,
    paddingBottom: 30,
    paddingHorizontal: theme.spacing.lg,
    borderBottomLeftRadius: theme.borderRadius.xl,
    borderBottomRightRadius: theme.borderRadius.xl,
  },
  headerContent: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  headerGreeting: {
    color: theme.colors.white,
    fontSize: 16,
    opacity: 0.9,
  },
  headerTitle: {
    color: theme.colors.white,
    fontSize: 24,
    fontWeight: 'bold',
    marginTop: 4,
  },
  profileButton: {
    borderRadius: theme.borderRadius.full,
    backgroundColor: 'rgba(255,255,255,0.2)',
    padding: 2,
  },

  // Content
  content: {
    flex: 1,
    paddingHorizontal: theme.spacing.lg,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: theme.colors.dark,
    marginBottom: theme.spacing.md,
  },

  // Status Card
  statusCard: {
    marginTop: theme.spacing.lg,
    borderRadius: theme.borderRadius.xl,
    shadowColor: theme.colors.dark,
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.1,
    shadowRadius: 8,
    elevation: 4,
  },
  statusGradient: {
    padding: theme.spacing.lg,
    borderRadius: theme.borderRadius.xl,
  },
  statusContent: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  statusLeft: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  onlineIndicator: {
    width: 50,
    height: 50,
    borderRadius: 25,
    backgroundColor: 'rgba(255,255,255,0.3)',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: theme.spacing.md,
  },
  onlineIndicatorInner: {
    width: 20,
    height: 20,
    borderRadius: 10,
  },
  statusTitle: {
    color: theme.colors.white,
    fontSize: 18,
    fontWeight: 'bold',
  },
  statusSubtitle: {
    color: theme.colors.white,
    fontSize: 14,
    opacity: 0.9,
    marginTop: 2,
  },

  // Stats Container
  statsContainer: {
    flexDirection: 'row',
    marginTop: theme.spacing.lg,
    gap: theme.spacing.sm,
  },
  statCard: {
    flex: 1,
    borderRadius: theme.borderRadius.lg,
    shadowColor: theme.colors.dark,
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 2,
  },
  statGradient: {
    padding: theme.spacing.md,
    borderRadius: theme.borderRadius.lg,
    alignItems: 'center',
  },
  statValue: {
    color: theme.colors.white,
    fontSize: 20,
    fontWeight: 'bold',
    marginTop: theme.spacing.xs,
  },
  statLabel: {
    color: theme.colors.white,
    fontSize: 12,
    opacity: 0.9,
    marginTop: 2,
    textAlign: 'center',
  },

  // Map Container
  mapContainer: {
    marginTop: theme.spacing.lg,
  },
  mapWrapper: {
    height: 200,
    borderRadius: theme.borderRadius.lg,
    overflow: 'hidden',
    position: 'relative',
  },
  map: {
    flex: 1,
  },
  driverMarker: {
    backgroundColor: theme.colors.primary,
    borderRadius: theme.borderRadius.full,
    padding: theme.spacing.sm,
    shadowColor: theme.colors.dark,
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.3,
    shadowRadius: 4,
    elevation: 4,
  },
  onlineOverlay: {
    position: 'absolute',
    top: theme.spacing.md,
    left: theme.spacing.md,
  },
  onlineInfo: {
    paddingHorizontal: theme.spacing.md,
    paddingVertical: theme.spacing.sm,
    borderRadius: theme.borderRadius.lg,
  },
  onlineInfoText: {
    color: theme.colors.success,
    fontWeight: 'bold',
    fontSize: 14,
  },

  // Requests Container
  requestsContainer: {
    marginTop: theme.spacing.lg,
    flex: 1,
  },
  offlineMessage: {
    alignItems: 'center',
    padding: theme.spacing.xl,
    backgroundColor: theme.colors.white,
    borderRadius: theme.borderRadius.lg,
    shadowColor: theme.colors.dark,
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 2,
  },
  offlineText: {
    fontSize: 18,
    fontWeight: 'bold',
    color: theme.colors.dark,
    marginTop: theme.spacing.md,
  },
  offlineSubtext: {
    fontSize: 14,
    color: theme.colors.gray,
    textAlign: 'center',
    marginTop: theme.spacing.sm,
  },

  // Request Card
  requestCard: {
    marginBottom: theme.spacing.md,
    borderRadius: theme.borderRadius.lg,
    shadowColor: theme.colors.dark,
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.1,
    shadowRadius: 8,
    elevation: 4,
  },
  requestBlur: {
    padding: theme.spacing.lg,
    borderRadius: theme.borderRadius.lg,
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.2)',
  },
  requestHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: theme.spacing.md,
  },
  passengerInfo: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  passengerDetails: {
    marginLeft: theme.spacing.sm,
  },
  passengerName: {
    fontSize: 16,
    fontWeight: 'bold',
    color: theme.colors.dark,
  },
  ratingContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 2,
  },
  passengerRating: {
    fontSize: 14,
    color: theme.colors.warning,
    marginLeft: 2,
    fontWeight: '600',
  },
  waitTime: {
    backgroundColor: theme.colors.warning,
    paddingHorizontal: theme.spacing.sm,
    paddingVertical: theme.spacing.xs,
    borderRadius: theme.borderRadius.md,
  },
  waitTimeText: {
    color: theme.colors.white,
    fontSize: 12,
    fontWeight: 'bold',
  },

  // Route Info
  routeInfo: {
    flexDirection: 'row',
    marginBottom: theme.spacing.md,
  },
  routeDots: {
    alignItems: 'center',
    marginRight: theme.spacing.md,
  },
  dot: {
    width: 8,
    height: 8,
    borderRadius: 4,
  },
  connectionLine: {
    width: 2,
    height: 20,
    backgroundColor: theme.colors.gray,
    opacity: 0.3,
    marginVertical: 4,
  },
  routeTexts: {
    flex: 1,
  },
  routeText: {
    fontSize: 14,
    color: theme.colors.dark,
    marginVertical: 4,
  },

  // Request Footer
  requestFooter: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  requestDetails: {
    flex: 1,
  },
  distanceText: {
    fontSize: 14,
    color: theme.colors.gray,
  },
  earningText: {
    fontSize: 18,
    fontWeight: 'bold',
    color: theme.colors.success,
    marginTop: 2,
  },
  requestActions: {
    flexDirection: 'row',
    gap: theme.spacing.sm,
  },
  declineButton: {
    width: 40,
    height: 40,
    borderRadius: theme.borderRadius.full,
    backgroundColor: theme.colors.white,
    justifyContent: 'center',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: theme.colors.danger,
  },
  acceptButton: {
    borderRadius: theme.borderRadius.lg,
    shadowColor: theme.colors.success,
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.3,
    shadowRadius: 4,
    elevation: 2,
  },
  acceptButtonGradient: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: theme.spacing.md,
    paddingVertical: theme.spacing.sm,
    borderRadius: theme.borderRadius.lg,
  },
  acceptButtonText: {
    color: theme.colors.white,
    fontWeight: 'bold',
    marginLeft: theme.spacing.xs,
  },

  bottomSpacer: {
    height: 100,
  },
});

export default DriverDashboard;