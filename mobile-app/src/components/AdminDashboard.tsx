import React, { useState, useEffect } from 'react';
import {
  StyleSheet,
  View,
  Text,
  TouchableOpacity,
  ScrollView,
  TextInput,
  Dimensions,
  StatusBar,
  Alert,
  Modal,
  FlatList
} from 'react-native';
import LinearGradient from 'react-native-linear-gradient';
import Icon from 'react-native-vector-icons/MaterialIcons';
import { BlurView } from '@react-native-blur/blur';
import Animated, { 
  useSharedValue, 
  useAnimatedStyle, 
  withSpring,
  withTiming 
} from 'react-native-reanimated';

const { width, height } = Dimensions.get('window');

// Admin Dashboard Theme
const theme = {
  colors: {
    primary: '#1E40AF', // Deep Blue
    secondary: '#7C3AED', // Purple
    success: '#059669', // Green
    warning: '#D97706', // Orange
    danger: '#DC2626', // Red
    dark: '#111827',
    light: '#F8FAFC',
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
 * Modern Admin Dashboard Interface
 */
const AdminDashboard: React.FC = () => {
  const [activeTab, setActiveTab] = useState('overview');
  const [showModal, setShowModal] = useState(false);
  const [modalType, setModalType] = useState('');
  const [searchQuery, setSearchQuery] = useState('');

  // Animated values
  const tabIndicatorPosition = useSharedValue(0);
  const modalScale = useSharedValue(0);

  // Sample data
  const stats = {
    totalRides: 15847,
    activeDrivers: 892,
    totalRevenue: 234567.89,
    userRating: 4.7,
    completionRate: 97.3
  };

  const recentRides = [
    { id: 1, user: 'Ahmet K.', driver: 'Mehmet S.', route: 'Taksim → Kadıköy', status: 'completed', amount: '₺45' },
    { id: 2, user: 'Zeynep A.', driver: 'Ali T.', route: 'Beşiktaş → Levent', status: 'ongoing', amount: '₺28' },
    { id: 3, user: 'Can M.', driver: 'Fatma K.', route: 'Şişli → Maslak', status: 'cancelled', amount: '₺0' },
  ];

  const drivers = [
    { id: 1, name: 'Mehmet Şahin', status: 'online', rating: 4.9, totalRides: 1247, earnings: '₺15,670' },
    { id: 2, name: 'Ali Taş', status: 'busy', rating: 4.7, totalRides: 892, earnings: '₺11,230' },
    { id: 3, name: 'Fatma Kaya', status: 'offline', rating: 4.8, totalRides: 1056, earnings: '₺13,450' },
  ];

  const users = [
    { id: 1, name: 'Ahmet Kılıç', email: 'ahmet@email.com', totalRides: 23, rating: 4.6, status: 'active' },
    { id: 2, name: 'Zeynep Acar', email: 'zeynep@email.com', totalRides: 45, rating: 4.8, status: 'active' },
    { id: 3, name: 'Can Mert', email: 'can@email.com', totalRides: 12, rating: 4.2, status: 'inactive' },
  ];

  const tabs = [
    { id: 'overview', title: 'Genel Bakış', icon: 'dashboard' },
    { id: 'rides', title: 'Yolculuklar', icon: 'local-taxi' },
    { id: 'drivers', title: 'Sürücüler', icon: 'people' },
    { id: 'users', title: 'Kullanıcılar', icon: 'person' },
    { id: 'analytics', title: 'Analitik', icon: 'analytics' },
  ];

  useEffect(() => {
    const activeIndex = tabs.findIndex(tab => tab.id === activeTab);
    tabIndicatorPosition.value = withSpring(activeIndex * (width / tabs.length));
  }, [activeTab]);

  const tabIndicatorStyle = useAnimatedStyle(() => {
    return {
      transform: [{ translateX: tabIndicatorPosition.value }],
    };
  });

  const openModal = (type: string) => {
    setModalType(type);
    setShowModal(true);
    modalScale.value = withSpring(1);
  };

  const closeModal = () => {
    modalScale.value = withTiming(0, {}, () => {
      setShowModal(false);
      setModalType('');
    });
  };

  const modalStyle = useAnimatedStyle(() => {
    return {
      transform: [{ scale: modalScale.value }],
    };
  });

  const renderHeader = () => (
    <LinearGradient
      colors={[theme.colors.primary, theme.colors.secondary]}
      style={styles.header}
    >
      <View style={styles.headerContent}>
        <View>
          <Text style={styles.headerGreeting}>WOLTAXI Admin 👨‍💼</Text>
          <Text style={styles.headerTitle}>Yönetim Paneli</Text>
        </View>
        <View style={styles.headerActions}>
          <TouchableOpacity style={styles.notificationButton}>
            <Icon name="notifications" size={24} color={theme.colors.white} />
            <View style={styles.notificationBadge}>
              <Text style={styles.notificationBadgeText}>3</Text>
            </View>
          </TouchableOpacity>
          <TouchableOpacity style={styles.profileButton}>
            <Icon name="admin-panel-settings" size={32} color={theme.colors.white} />
          </TouchableOpacity>
        </View>
      </View>
    </LinearGradient>
  );

  const renderTabs = () => (
    <View style={styles.tabsContainer}>
      <ScrollView horizontal showsHorizontalScrollIndicator={false}>
        <View style={styles.tabsWrapper}>
          <Animated.View style={[styles.tabIndicator, tabIndicatorStyle]} />
          {tabs.map((tab) => (
            <TouchableOpacity
              key={tab.id}
              style={styles.tab}
              onPress={() => setActiveTab(tab.id)}
            >
              <Icon 
                name={tab.icon} 
                size={20} 
                color={activeTab === tab.id ? theme.colors.primary : theme.colors.gray} 
              />
              <Text style={[
                styles.tabText,
                { color: activeTab === tab.id ? theme.colors.primary : theme.colors.gray }
              ]}>
                {tab.title}
              </Text>
            </TouchableOpacity>
          ))}
        </View>
      </ScrollView>
    </View>
  );

  const renderStatsOverview = () => (
    <View style={styles.statsContainer}>
      <View style={styles.statsGrid}>
        <View style={styles.statCard}>
          <LinearGradient colors={[theme.colors.primary, '#3B82F6']} style={styles.statGradient}>
            <Icon name="local-taxi" size={32} color={theme.colors.white} />
            <Text style={styles.statValue}>{stats.totalRides.toLocaleString()}</Text>
            <Text style={styles.statLabel}>Toplam Yolculuk</Text>
          </LinearGradient>
        </View>

        <View style={styles.statCard}>
          <LinearGradient colors={[theme.colors.success, '#10B981']} style={styles.statGradient}>
            <Icon name="people" size={32} color={theme.colors.white} />
            <Text style={styles.statValue}>{stats.activeDrivers}</Text>
            <Text style={styles.statLabel}>Aktif Sürücü</Text>
          </LinearGradient>
        </View>

        <View style={styles.statCard}>
          <LinearGradient colors={[theme.colors.warning, '#F59E0B']} style={styles.statGradient}>
            <Icon name="attach-money" size={32} color={theme.colors.white} />
            <Text style={styles.statValue}>₺{(stats.totalRevenue / 1000).toFixed(0)}K</Text>
            <Text style={styles.statLabel}>Toplam Gelir</Text>
          </LinearGradient>
        </View>

        <View style={styles.statCard}>
          <LinearGradient colors={[theme.colors.secondary, '#8B5CF6']} style={styles.statGradient}>
            <Icon name="star" size={32} color={theme.colors.white} />
            <Text style={styles.statValue}>{stats.userRating}</Text>
            <Text style={styles.statLabel}>Ortalama Puan</Text>
          </LinearGradient>
        </View>
      </View>
    </View>
  );

  const renderRecentActivity = () => (
    <View style={styles.sectionContainer}>
      <View style={styles.sectionHeader}>
        <Text style={styles.sectionTitle}>📊 Son Aktiviteler</Text>
        <TouchableOpacity onPress={() => openModal('activity')}>
          <Text style={styles.sectionLink}>Tümünü Gör</Text>
        </TouchableOpacity>
      </View>
      
      {recentRides.map((ride) => (
        <View key={ride.id} style={styles.activityCard}>
          <View style={styles.activityIcon}>
            <Icon 
              name="local-taxi" 
              size={20} 
              color={ride.status === 'completed' ? theme.colors.success : 
                     ride.status === 'ongoing' ? theme.colors.warning : theme.colors.danger} 
            />
          </View>
          <View style={styles.activityContent}>
            <Text style={styles.activityTitle}>{ride.user} → {ride.driver}</Text>
            <Text style={styles.activitySubtitle}>{ride.route}</Text>
          </View>
          <View style={styles.activityMeta}>
            <Text style={[
              styles.activityStatus,
              { color: ride.status === 'completed' ? theme.colors.success : 
                       ride.status === 'ongoing' ? theme.colors.warning : theme.colors.danger }
            ]}>
              {ride.status === 'completed' ? 'Tamamlandı' : 
               ride.status === 'ongoing' ? 'Devam Ediyor' : 'İptal Edildi'}
            </Text>
            <Text style={styles.activityAmount}>{ride.amount}</Text>
          </View>
        </View>
      ))}
    </View>
  );

  const renderDriversList = () => (
    <View style={styles.sectionContainer}>
      <View style={styles.sectionHeader}>
        <Text style={styles.sectionTitle}>🚗 Sürücü Yönetimi</Text>
        <TouchableOpacity 
          style={styles.addButton}
          onPress={() => openModal('addDriver')}
        >
          <Icon name="add" size={20} color={theme.colors.white} />
        </TouchableOpacity>
      </View>

      {drivers.map((driver) => (
        <View key={driver.id} style={styles.driverCard}>
          <View style={styles.driverInfo}>
            <View style={[
              styles.driverStatus,
              { backgroundColor: driver.status === 'online' ? theme.colors.success :
                               driver.status === 'busy' ? theme.colors.warning : theme.colors.gray }
            ]} />
            <View style={styles.driverDetails}>
              <Text style={styles.driverName}>{driver.name}</Text>
              <Text style={styles.driverStats}>
                {driver.totalRides} yolculuk • {driver.earnings}
              </Text>
            </View>
          </View>
          <View style={styles.driverRating}>
            <Icon name="star" size={16} color={theme.colors.warning} />
            <Text style={styles.driverRatingText}>{driver.rating}</Text>
          </View>
        </View>
      ))}
    </View>
  );

  const renderUsersList = () => (
    <View style={styles.sectionContainer}>
      <View style={styles.sectionHeader}>
        <Text style={styles.sectionTitle}>👥 Kullanıcı Yönetimi</Text>
        <View style={styles.searchContainer}>
          <Icon name="search" size={20} color={theme.colors.gray} />
          <TextInput
            style={styles.searchInput}
            placeholder="Kullanıcı ara..."
            value={searchQuery}
            onChangeText={setSearchQuery}
            placeholderTextColor={theme.colors.gray}
          />
        </View>
      </View>

      {users.map((user) => (
        <View key={user.id} style={styles.userCard}>
          <View style={styles.userAvatar}>
            <Text style={styles.userAvatarText}>{user.name.charAt(0)}</Text>
          </View>
          <View style={styles.userInfo}>
            <Text style={styles.userName}>{user.name}</Text>
            <Text style={styles.userEmail}>{user.email}</Text>
            <Text style={styles.userStats}>
              {user.totalRides} yolculuk • ⭐ {user.rating}
            </Text>
          </View>
          <View style={[
            styles.userStatus,
            { backgroundColor: user.status === 'active' ? theme.colors.success : theme.colors.gray }
          ]}>
            <Text style={styles.userStatusText}>
              {user.status === 'active' ? 'Aktif' : 'Pasif'}
            </Text>
          </View>
        </View>
      ))}
    </View>
  );

  const renderAnalytics = () => (
    <View style={styles.sectionContainer}>
      <Text style={styles.sectionTitle}>📈 Analitik Veriler</Text>
      
      <View style={styles.analyticsCard}>
        <LinearGradient
          colors={['#667eea', '#764ba2']}
          style={styles.analyticsGradient}
        >
          <Text style={styles.analyticsTitle}>Günlük Performans</Text>
          <View style={styles.analyticsStats}>
            <View style={styles.analyticsStat}>
              <Text style={styles.analyticsValue}>97.3%</Text>
              <Text style={styles.analyticsLabel}>Tamamlanma Oranı</Text>
            </View>
            <View style={styles.analyticsStat}>
              <Text style={styles.analyticsValue}>4.2dk</Text>
              <Text style={styles.analyticsLabel}>Ortalama Bekleme</Text>
            </View>
          </View>
        </LinearGradient>
      </View>

      <View style={styles.analyticsCard}>
        <LinearGradient
          colors={['#f093fb', '#f5576c']}
          style={styles.analyticsGradient}
        >
          <Text style={styles.analyticsTitle}>Haftalık Trend</Text>
          <View style={styles.analyticsStats}>
            <View style={styles.analyticsStat}>
              <Text style={styles.analyticsValue}>+12%</Text>
              <Text style={styles.analyticsLabel}>Yolculuk Artışı</Text>
            </View>
            <View style={styles.analyticsStat}>
              <Text style={styles.analyticsValue}>+8.5%</Text>
              <Text style={styles.analyticsLabel}>Gelir Artışı</Text>
            </View>
          </View>
        </LinearGradient>
      </View>
    </View>
  );

  const renderContent = () => {
    switch (activeTab) {
      case 'overview':
        return (
          <>
            {renderStatsOverview()}
            {renderRecentActivity()}
          </>
        );
      case 'rides':
        return renderRecentActivity();
      case 'drivers':
        return renderDriversList();
      case 'users':
        return renderUsersList();
      case 'analytics':
        return renderAnalytics();
      default:
        return renderStatsOverview();
    }
  };

  const renderModal = () => (
    <Modal visible={showModal} transparent animationType="fade">
      <BlurView style={styles.modalOverlay} blurType="dark" blurAmount={10}>
        <Animated.View style={[styles.modalContent, modalStyle]}>
          <View style={styles.modalHeader}>
            <Text style={styles.modalTitle}>
              {modalType === 'addDriver' ? 'Yeni Sürücü Ekle' : 'Detaylar'}
            </Text>
            <TouchableOpacity onPress={closeModal}>
              <Icon name="close" size={24} color={theme.colors.gray} />
            </TouchableOpacity>
          </View>
          
          <View style={styles.modalBody}>
            <Text style={styles.modalText}>
              {modalType === 'addDriver' 
                ? 'Yeni sürücü ekleme formu burada görünecek.'
                : 'Detaylı bilgiler burada gösterilecek.'
              }
            </Text>
          </View>
          
          <TouchableOpacity style={styles.modalButton} onPress={closeModal}>
            <LinearGradient
              colors={[theme.colors.primary, theme.colors.secondary]}
              style={styles.modalButtonGradient}
            >
              <Text style={styles.modalButtonText}>
                {modalType === 'addDriver' ? 'Kaydet' : 'Tamam'}
              </Text>
            </LinearGradient>
          </TouchableOpacity>
        </Animated.View>
      </BlurView>
    </Modal>
  );

  return (
    <View style={styles.container}>
      <StatusBar backgroundColor={theme.colors.primary} barStyle="light-content" />
      
      {renderHeader()}
      {renderTabs()}
      
      <ScrollView style={styles.content} showsVerticalScrollIndicator={false}>
        {renderContent()}
        <View style={styles.bottomSpacer} />
      </ScrollView>
      
      {renderModal()}
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
    paddingBottom: 20,
    paddingHorizontal: theme.spacing.lg,
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
  headerActions: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: theme.spacing.md,
  },
  notificationButton: {
    position: 'relative',
    padding: theme.spacing.sm,
  },
  notificationBadge: {
    position: 'absolute',
    top: 4,
    right: 4,
    backgroundColor: theme.colors.danger,
    borderRadius: theme.borderRadius.full,
    width: 18,
    height: 18,
    justifyContent: 'center',
    alignItems: 'center',
  },
  notificationBadgeText: {
    color: theme.colors.white,
    fontSize: 10,
    fontWeight: 'bold',
  },
  profileButton: {
    backgroundColor: 'rgba(255,255,255,0.2)',
    borderRadius: theme.borderRadius.full,
    padding: theme.spacing.xs,
  },

  // Tabs Styles
  tabsContainer: {
    backgroundColor: theme.colors.white,
    shadowColor: theme.colors.dark,
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 2,
  },
  tabsWrapper: {
    flexDirection: 'row',
    position: 'relative',
  },
  tabIndicator: {
    position: 'absolute',
    bottom: 0,
    height: 3,
    width: width / 5,
    backgroundColor: theme.colors.primary,
    borderRadius: theme.borderRadius.sm,
  },
  tab: {
    width: width / 5,
    paddingVertical: theme.spacing.md,
    alignItems: 'center',
    gap: theme.spacing.xs,
  },
  tabText: {
    fontSize: 12,
    fontWeight: '600',
  },

  // Content
  content: {
    flex: 1,
    paddingHorizontal: theme.spacing.lg,
  },

  // Stats Overview
  statsContainer: {
    marginTop: theme.spacing.lg,
  },
  statsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: theme.spacing.sm,
  },
  statCard: {
    width: (width - theme.spacing.lg * 2 - theme.spacing.sm) / 2,
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
    fontSize: 24,
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

  // Section Styles
  sectionContainer: {
    marginTop: theme.spacing.lg,
    backgroundColor: theme.colors.white,
    borderRadius: theme.borderRadius.lg,
    padding: theme.spacing.md,
    shadowColor: theme.colors.dark,
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 2,
  },
  sectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: theme.spacing.md,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: theme.colors.dark,
  },
  sectionLink: {
    color: theme.colors.primary,
    fontSize: 14,
    fontWeight: '600',
  },
  addButton: {
    backgroundColor: theme.colors.primary,
    borderRadius: theme.borderRadius.full,
    width: 32,
    height: 32,
    justifyContent: 'center',
    alignItems: 'center',
  },

  // Activity Card
  activityCard: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: theme.spacing.sm,
    borderBottomWidth: 1,
    borderBottomColor: theme.colors.light,
  },
  activityIcon: {
    width: 40,
    height: 40,
    borderRadius: theme.borderRadius.full,
    backgroundColor: theme.colors.light,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: theme.spacing.md,
  },
  activityContent: {
    flex: 1,
  },
  activityTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: theme.colors.dark,
  },
  activitySubtitle: {
    fontSize: 14,
    color: theme.colors.gray,
    marginTop: 2,
  },
  activityMeta: {
    alignItems: 'flex-end',
  },
  activityStatus: {
    fontSize: 12,
    fontWeight: '600',
  },
  activityAmount: {
    fontSize: 16,
    fontWeight: 'bold',
    color: theme.colors.dark,
    marginTop: 2,
  },

  // Driver Card
  driverCard: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: theme.spacing.md,
    borderBottomWidth: 1,
    borderBottomColor: theme.colors.light,
  },
  driverInfo: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
  },
  driverStatus: {
    width: 12,
    height: 12,
    borderRadius: theme.borderRadius.full,
    marginRight: theme.spacing.md,
  },
  driverDetails: {
    flex: 1,
  },
  driverName: {
    fontSize: 16,
    fontWeight: '600',
    color: theme.colors.dark,
  },
  driverStats: {
    fontSize: 14,
    color: theme.colors.gray,
    marginTop: 2,
  },
  driverRating: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 2,
  },
  driverRatingText: {
    fontSize: 14,
    fontWeight: '600',
    color: theme.colors.warning,
  },

  // Search Container
  searchContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: theme.colors.light,
    borderRadius: theme.borderRadius.lg,
    paddingHorizontal: theme.spacing.md,
    paddingVertical: theme.spacing.sm,
    gap: theme.spacing.sm,
    width: 200,
  },
  searchInput: {
    flex: 1,
    fontSize: 14,
    color: theme.colors.dark,
  },

  // User Card
  userCard: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: theme.spacing.md,
    borderBottomWidth: 1,
    borderBottomColor: theme.colors.light,
  },
  userAvatar: {
    width: 40,
    height: 40,
    borderRadius: theme.borderRadius.full,
    backgroundColor: theme.colors.primary,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: theme.spacing.md,
  },
  userAvatarText: {
    color: theme.colors.white,
    fontSize: 16,
    fontWeight: 'bold',
  },
  userInfo: {
    flex: 1,
  },
  userName: {
    fontSize: 16,
    fontWeight: '600',
    color: theme.colors.dark,
  },
  userEmail: {
    fontSize: 14,
    color: theme.colors.gray,
    marginTop: 2,
  },
  userStats: {
    fontSize: 12,
    color: theme.colors.gray,
    marginTop: 2,
  },
  userStatus: {
    paddingHorizontal: theme.spacing.sm,
    paddingVertical: theme.spacing.xs,
    borderRadius: theme.borderRadius.md,
  },
  userStatusText: {
    color: theme.colors.white,
    fontSize: 12,
    fontWeight: 'bold',
  },

  // Analytics
  analyticsCard: {
    marginBottom: theme.spacing.md,
    borderRadius: theme.borderRadius.lg,
    shadowColor: theme.colors.dark,
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 2,
  },
  analyticsGradient: {
    padding: theme.spacing.lg,
    borderRadius: theme.borderRadius.lg,
  },
  analyticsTitle: {
    color: theme.colors.white,
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: theme.spacing.md,
  },
  analyticsStats: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  analyticsStat: {
    alignItems: 'center',
  },
  analyticsValue: {
    color: theme.colors.white,
    fontSize: 24,
    fontWeight: 'bold',
  },
  analyticsLabel: {
    color: theme.colors.white,
    fontSize: 12,
    opacity: 0.9,
    marginTop: 4,
  },

  // Modal Styles
  modalOverlay: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: theme.spacing.lg,
  },
  modalContent: {
    backgroundColor: theme.colors.white,
    borderRadius: theme.borderRadius.xl,
    padding: theme.spacing.lg,
    width: '100%',
    maxWidth: 400,
    shadowColor: theme.colors.dark,
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.2,
    shadowRadius: 16,
    elevation: 10,
  },
  modalHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: theme.spacing.lg,
  },
  modalTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: theme.colors.dark,
  },
  modalBody: {
    marginBottom: theme.spacing.lg,
  },
  modalText: {
    fontSize: 16,
    color: theme.colors.gray,
    textAlign: 'center',
  },
  modalButton: {
    borderRadius: theme.borderRadius.lg,
  },
  modalButtonGradient: {
    paddingVertical: theme.spacing.md,
    paddingHorizontal: theme.spacing.lg,
    borderRadius: theme.borderRadius.lg,
    alignItems: 'center',
  },
  modalButtonText: {
    color: theme.colors.white,
    fontSize: 16,
    fontWeight: 'bold',
  },

  bottomSpacer: {
    height: 100,
  },
});

export default AdminDashboard;