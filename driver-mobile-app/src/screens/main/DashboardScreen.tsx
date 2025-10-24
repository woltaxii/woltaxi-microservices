/**
 * WOLTAXI Driver Dashboard Screen
 * 
 * Sürücü ana sayfa - Abonelik durumu, günlük kazanç, müşteri portföyü özeti
 */

import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  ScrollView,
  TouchableOpacity,
  StyleSheet,
  Dimensions,
  RefreshControl,
} from 'react-native';
import Icon from 'react-native-vector-icons/MaterialIcons';
import LinearGradient from 'react-native-linear-gradient';

const { width } = Dimensions.get('window');

interface DashboardScreenProps {
  navigation: any;
}

interface DashboardData {
  currentSubscription: {
    packageName: string;
    daysRemaining: number;
    ridesUsed: number;
    ridesLimit: number;
  };
  todayStats: {
    earnings: number;
    rides: number;
    customers: number;
  };
  weeklyStats: {
    earnings: number;
    rides: number;
    avgRating: number;
  };
  portfolioStats: {
    totalCustomers: number;
    activeCustomers: number;
    limit: number;
  };
}

const DashboardScreen: React.FC<DashboardScreenProps> = ({ navigation }) => {
  const [dashboardData, setDashboardData] = useState<DashboardData | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  useEffect(() => {
    loadDashboardData();
  }, []);

  /**
   * Dashboard Verilerini Yükle
   */
  const loadDashboardData = async () => {
    try {
      // Mock data - Gerçek uygulamada API'den gelecek
      const mockData: DashboardData = {
        currentSubscription: {
          packageName: 'PREMIUM',
          daysRemaining: 15,
          ridesUsed: 342,
          ridesLimit: 1500,
        },
        todayStats: {
          earnings: 485.50,
          rides: 12,
          customers: 8,
        },
        weeklyStats: {
          earnings: 2840.75,
          rides: 84,
          avgRating: 4.8,
        },
        portfolioStats: {
          totalCustomers: 127,
          activeCustomers: 89,
          limit: 150,
        },
      };

      setDashboardData(mockData);
    } catch (error) {
      console.error('Dashboard data load error:', error);
    } finally {
      setIsLoading(false);
      setRefreshing(false);
    }
  };

  /**
   * Yenileme İşlemi
   */
  const onRefresh = () => {
    setRefreshing(true);
    loadDashboardData();
  };

  if (isLoading || !dashboardData) {
    return (
      <View style={styles.loadingContainer}>
        <Text>Yükleniyor...</Text>
      </View>
    );
  }

  return (
    <ScrollView
      style={styles.container}
      refreshControl={
        <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
      }
    >
      {/* Header Welcome */}
      <LinearGradient
        colors={['#E30613', '#FF6B6B']}
        style={styles.welcomeHeader}
      >
        <View style={styles.welcomeContent}>
          <Text style={styles.welcomeText}>Hoş Geldiniz!</Text>
          <Text style={styles.driverName}>Mehmet YILMAZ</Text>
          <Text style={styles.subscriptionBadge}>
            {dashboardData.currentSubscription.packageName} PAKET
          </Text>
        </View>
      </LinearGradient>

      {/* Subscription Status Card */}
      <View style={styles.card}>
        <View style={styles.cardHeader}>
          <Icon name="card-membership" size={24} color="#E30613" />
          <Text style={styles.cardTitle}>Abonelik Durumu</Text>
        </View>
        <View style={styles.subscriptionContent}>
          <View style={styles.subscriptionRow}>
            <Text style={styles.subscriptionLabel}>Kalan Gün:</Text>
            <Text style={styles.subscriptionValue}>
              {dashboardData.currentSubscription.daysRemaining} gün
            </Text>
          </View>
          <View style={styles.subscriptionRow}>
            <Text style={styles.subscriptionLabel}>Kullanılan Yolculuk:</Text>
            <Text style={styles.subscriptionValue}>
              {dashboardData.currentSubscription.ridesUsed} / {dashboardData.currentSubscription.ridesLimit}
            </Text>
          </View>
          <View style={styles.progressBar}>
            <View 
              style={[
                styles.progressFill, 
                { width: `${(dashboardData.currentSubscription.ridesUsed / dashboardData.currentSubscription.ridesLimit) * 100}%` }
              ]} 
            />
          </View>
        </View>
        <TouchableOpacity 
          style={styles.subscriptionButton}
          onPress={() => navigation.navigate('Subscription')}
        >
          <Text style={styles.subscriptionButtonText}>Paketi Yönet</Text>
        </TouchableOpacity>
      </View>

      {/* Today's Stats */}
      <Text style={styles.sectionTitle}>Bugünkü Performans</Text>
      <View style={styles.statsRow}>
        <View style={[styles.statCard, { backgroundColor: '#4CAF50' }]}>
          <Icon name="account-balance-wallet" size={28} color="white" />
          <Text style={styles.statValue}>₺{dashboardData.todayStats.earnings}</Text>
          <Text style={styles.statLabel}>Kazanç</Text>
        </View>
        <View style={[styles.statCard, { backgroundColor: '#2196F3' }]}>
          <Icon name="local-taxi" size={28} color="white" />
          <Text style={styles.statValue}>{dashboardData.todayStats.rides}</Text>
          <Text style={styles.statLabel}>Yolculuk</Text>
        </View>
        <View style={[styles.statCard, { backgroundColor: '#FF9800' }]}>
          <Icon name="people" size={28} color="white" />
          <Text style={styles.statValue}>{dashboardData.todayStats.customers}</Text>
          <Text style={styles.statLabel}>Müşteri</Text>
        </View>
      </View>

      {/* Weekly Performance */}
      <Text style={styles.sectionTitle}>Haftalık Özet</Text>
      <View style={styles.card}>
        <View style={styles.weeklyStatsContainer}>
          <View style={styles.weeklyStatItem}>
            <Text style={styles.weeklyStatValue}>₺{dashboardData.weeklyStats.earnings}</Text>
            <Text style={styles.weeklyStatLabel}>Haftalık Kazanç</Text>
          </View>
          <View style={styles.weeklyStatItem}>
            <Text style={styles.weeklyStatValue}>{dashboardData.weeklyStats.rides}</Text>
            <Text style={styles.weeklyStatLabel}>Toplam Yolculuk</Text>
          </View>
          <View style={styles.weeklyStatItem}>
            <View style={styles.ratingContainer}>
              <Icon name="star" size={20} color="#FFD700" />
              <Text style={styles.weeklyStatValue}>{dashboardData.weeklyStats.avgRating}</Text>
            </View>
            <Text style={styles.weeklyStatLabel}>Ortalama Puan</Text>
          </View>
        </View>
      </View>

      {/* Customer Portfolio */}
      <Text style={styles.sectionTitle}>Müşteri Portföyü</Text>
      <TouchableOpacity 
        style={styles.card}
        onPress={() => navigation.navigate('Portfolio')}
      >
        <View style={styles.portfolioHeader}>
          <Icon name="people" size={24} color="#E30613" />
          <Text style={styles.cardTitle}>Müşteri Yönetimi</Text>
          <Icon name="chevron-right" size={24} color="#666" />
        </View>
        <View style={styles.portfolioStats}>
          <View style={styles.portfolioStatItem}>
            <Text style={styles.portfolioStatValue}>{dashboardData.portfolioStats.totalCustomers}</Text>
            <Text style={styles.portfolioStatLabel}>Toplam Müşteri</Text>
          </View>
          <View style={styles.portfolioStatItem}>
            <Text style={styles.portfolioStatValue}>{dashboardData.portfolioStats.activeCustomers}</Text>
            <Text style={styles.portfolioStatLabel}>Aktif Müşteri</Text>
          </View>
          <View style={styles.portfolioStatItem}>
            <Text style={styles.portfolioStatValue}>
              {dashboardData.portfolioStats.limit - dashboardData.portfolioStats.totalCustomers}
            </Text>
            <Text style={styles.portfolioStatLabel}>Kalan Kapasite</Text>
          </View>
        </View>
      </TouchableOpacity>

      {/* Quick Actions */}
      <Text style={styles.sectionTitle}>Hızlı İşlemler</Text>
      <View style={styles.quickActionsContainer}>
        <TouchableOpacity 
          style={styles.quickActionButton}
          onPress={() => navigation.navigate('Rides')}
        >
          <Icon name="local-taxi" size={32} color="#E30613" />
          <Text style={styles.quickActionText}>Yolculuklarım</Text>
        </TouchableOpacity>
        <TouchableOpacity 
          style={styles.quickActionButton}
          onPress={() => navigation.navigate('Earnings')}
        >
          <Icon name="trending-up" size={32} color="#4CAF50" />
          <Text style={styles.quickActionText}>Kazanç Raporu</Text>
        </TouchableOpacity>
        <TouchableOpacity 
          style={styles.quickActionButton}
          onPress={() => navigation.navigate('AddCustomer')}
        >
          <Icon name="person-add" size={32} color="#2196F3" />
          <Text style={styles.quickActionText}>Müşteri Ekle</Text>
        </TouchableOpacity>
        <TouchableOpacity 
          style={styles.quickActionButton}
          onPress={() => navigation.navigate('Packages')}
        >
          <Icon name="upgrade" size={32} color="#FF9800" />
          <Text style={styles.quickActionText}>Paket Yükselt</Text>
        </TouchableOpacity>
      </View>

      <View style={styles.bottomSpacer} />
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f8f9fa',
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  welcomeHeader: {
    padding: 25,
    paddingTop: 40,
  },
  welcomeContent: {
    alignItems: 'center',
  },
  welcomeText: {
    fontSize: 18,
    color: 'rgba(255, 255, 255, 0.9)',
    marginBottom: 5,
  },
  driverName: {
    fontSize: 24,
    fontWeight: 'bold',
    color: 'white',
    marginBottom: 10,
  },
  subscriptionBadge: {
    backgroundColor: 'rgba(255, 255, 255, 0.2)',
    paddingHorizontal: 15,
    paddingVertical: 5,
    borderRadius: 15,
    color: 'white',
    fontSize: 12,
    fontWeight: 'bold',
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
    marginTop: 25,
    marginBottom: 15,
    marginHorizontal: 20,
  },
  card: {
    backgroundColor: 'white',
    marginHorizontal: 20,
    marginBottom: 15,
    borderRadius: 12,
    padding: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 8,
    elevation: 3,
  },
  cardHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 15,
  },
  cardTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#333',
    marginLeft: 10,
    flex: 1,
  },
  subscriptionContent: {
    marginBottom: 15,
  },
  subscriptionRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 8,
  },
  subscriptionLabel: {
    fontSize: 14,
    color: '#666',
  },
  subscriptionValue: {
    fontSize: 14,
    fontWeight: 'bold',
    color: '#333',
  },
  progressBar: {
    height: 6,
    backgroundColor: '#E0E0E0',
    borderRadius: 3,
    marginTop: 10,
  },
  progressFill: {
    height: '100%',
    backgroundColor: '#E30613',
    borderRadius: 3,
  },
  subscriptionButton: {
    backgroundColor: '#E30613',
    borderRadius: 8,
    paddingVertical: 12,
    alignItems: 'center',
  },
  subscriptionButtonText: {
    color: 'white',
    fontSize: 14,
    fontWeight: 'bold',
  },
  statsRow: {
    flexDirection: 'row',
    paddingHorizontal: 20,
    justifyContent: 'space-between',
  },
  statCard: {
    flex: 1,
    borderRadius: 12,
    padding: 15,
    alignItems: 'center',
    marginHorizontal: 4,
  },
  statValue: {
    fontSize: 18,
    fontWeight: 'bold',
    color: 'white',
    marginTop: 8,
  },
  statLabel: {
    fontSize: 12,
    color: 'rgba(255, 255, 255, 0.9)',
    marginTop: 4,
  },
  weeklyStatsContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  weeklyStatItem: {
    alignItems: 'center',
  },
  weeklyStatValue: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#333',
  },
  weeklyStatLabel: {
    fontSize: 12,
    color: '#666',
    marginTop: 4,
    textAlign: 'center',
  },
  ratingContainer: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  portfolioHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 15,
  },
  portfolioStats: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  portfolioStatItem: {
    alignItems: 'center',
  },
  portfolioStatValue: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#E30613',
  },
  portfolioStatLabel: {
    fontSize: 12,
    color: '#666',
    marginTop: 4,
    textAlign: 'center',
  },
  quickActionsContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    paddingHorizontal: 20,
    justifyContent: 'space-between',
  },
  quickActionButton: {
    width: (width - 60) / 2,
    backgroundColor: 'white',
    borderRadius: 12,
    padding: 20,
    alignItems: 'center',
    marginBottom: 15,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 8,
    elevation: 3,
  },
  quickActionText: {
    fontSize: 14,
    fontWeight: '500',
    color: '#333',
    marginTop: 8,
    textAlign: 'center',
  },
  bottomSpacer: {
    height: 20,
  },
});

export default DashboardScreen;