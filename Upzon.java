import java.lang.Math; 
import java.util.Random;
import java.text.DecimalFormat;

public class Upzon {
    // Needed objects
    static Random rand = new Random();

    // Defined values
    static int DAYS = 120;
    static int OBSERVATIONS = 10000;
    static int SHOW_PROBABILITY = 75;

    // Values used for simulation
    static double compensation;
    static int acceptProb;
    static double distFact;

    // Best compensation for couriers
    static double bestVal;
    static double bestComp;
    static int bestProb;

    // New Packages
    static int[][] npHome = new int[OBSERVATIONS][DAYS];
    static int[][] npLocker = new int[OBSERVATIONS][DAYS]; 

    // Deliveries
    static int[][] delivPf = new int[OBSERVATIONS][DAYS];
    static int[][] delivOc = new int[OBSERVATIONS][DAYS];
    static int[][] delivLocker = new int[OBSERVATIONS][DAYS];

    // Accumulated deliveries 
    static int[][] accDelivPf = new int[OBSERVATIONS][DAYS];
    static int[][] accDelivOc = new int[OBSERVATIONS][DAYS];
    static int[][] accDelivLocker = new int[OBSERVATIONS][DAYS];

    // Costs
    static double[][] costPf = new double[OBSERVATIONS][DAYS];
    static double[][] costOc = new double[OBSERVATIONS][DAYS];

    // Accumulated costs
    static double[][] totalCost = new double[OBSERVATIONS][DAYS];

    // Locker Status
    static int[][] statusHome = new int[OBSERVATIONS][DAYS];
    static int[][] statusLocker = new int[OBSERVATIONS][DAYS];
    static int[] statusMax = new int[OBSERVATIONS];

    static void calculateDistFact() {
        distFact = 2.576321;
    }

    static void simulation() {
        for ( int obs = 0; obs < OBSERVATIONS; obs++ ) {
            simulateFirstDay( obs );

            for( int day = 1; day < DAYS; day++ ) {    
                // Deal with previous day requests
                int home = statusHome[obs][day-1];
                int locker = statusLocker[obs][day-1];

                int homeCompleted = 0;
                int lockerCompleted = 0;
                double costRecipient = 0.0;

                // Check recipients in locker
                for( int i = 0; i < locker; i++ ) {
                    // Check if recipient came to the locker
                    boolean showedUp = recipientShowedUp();
                    if( showedUp ) {
                        lockerCompleted++;

                        // No packages to deliver home
                        if( home - homeCompleted <= 0 ) {
                            continue;
                        }

                        // Check if recipient wants to deliver a package
                        boolean toDeliver = recipientWantsToDeliver();
                        if( toDeliver ) {
                            homeCompleted++;
                        }
                    }
                }

                // Update values from deliveries made by recipients
                costRecipient = compensation * homeCompleted;

                delivLocker[obs][day] = lockerCompleted;
                delivOc[obs][day] = homeCompleted;

                accDelivLocker[obs][day] = accDelivLocker[obs][day-1] + lockerCompleted;
                accDelivOc[obs][day] = accDelivOc[obs][day-1] + homeCompleted;

                costOc[obs][day] = costRecipient;

                // Send remaining packages using professionals and update values
                int homeIncompleted = home - homeCompleted;
                double costProfessional = 0.0;

                delivPf[obs][day] = homeIncompleted;
                accDelivPf[obs][day] = accDelivPf[obs][day-1] + homeIncompleted;

                if( homeIncompleted > 0) {
                    // Update delivery costs
                    if( homeIncompleted <= 10 ) {
                        costProfessional = (double) homeIncompleted * 1;
                    }
                    else {
                        costProfessional = (double) 10 * 1;
                        homeIncompleted = homeIncompleted - 10;
                        costProfessional = (double) costProfessional + homeIncompleted * 2;
                    }
                }

                costPf[obs][day] = costProfessional;

                // Update totalCost
                totalCost[obs][day] = totalCost[obs][day-1] + costRecipient + costProfessional;

                // Generate current day requests
                generateNewRequests( obs, day );
                statusHome[obs][day] = npHome[obs][day];
                statusLocker[obs][day] = npLocker[obs][day];

                // Add to statusLocker the packages that weren't delivered
                int lockerIncompleted = locker - lockerCompleted;
                if( lockerIncompleted > 0 ) {
                    statusLocker[obs][day] = statusLocker[obs][day] + lockerIncompleted;
                }

                if( statusMax[obs] < statusHome[obs][day] + statusLocker[obs][day] ) {
                    statusMax[obs] = statusHome[obs][day] + statusLocker[obs][day];
                }
            }
        }
    }

    static void simulateFirstDay( int obs ) {
        generateNewRequests( obs, 0 );
        statusHome[obs][0] = npHome[obs][0];
        statusLocker[obs][0] = npLocker[obs][0];

        // All other variables have value 0
        delivPf[obs][0] = 0;
        delivOc[obs][0] = 0;
        delivLocker[obs][0] = 0;

        accDelivPf[obs][0] = 0;
        accDelivOc[obs][0] = 0;
        accDelivLocker[obs][0] = 0;

        costPf[obs][0] = 0.0;
        costOc[obs][0] = 0.0;
        totalCost[obs][0] = 0.0;

        statusMax[obs] = statusHome[obs][0] + statusLocker[obs][0];
    }

    static void generateNewRequests( int obs, int day ) {
        int nr = generateNumberRequests();
        int home = 0;
        int locker = 0;
        for( int i = 0; i < nr; i++ ) {
            int type = generateRequestType();
            if( type == 0 ) {
                home++;
            }
            else if( type == 1 ) {
                locker++;
            }
            else {
                home = -10000;
                locker = -10000;
            }
        }
        npHome[obs][day] = home;
        npLocker[obs][day] = locker;
    }

    static int generateNumberRequests() {
        int ans = rand.nextInt( 41 ); 
        ans = ans + 10;
        return ans;
    }

    static int generateRequestType() {
        int ans = rand.nextInt( 2 );
        return ans;
    }

    static boolean recipientShowedUp() {
        int aux = rand.nextInt( 100 );
        boolean ans;
        if( aux < SHOW_PROBABILITY ) {
            ans = true;
        }
        else {
            ans = false;
        }
        return ans;
    }

    static boolean recipientWantsToDeliver() {
        int aux = rand.nextInt( 100 );
        boolean ans;
        if( aux < acceptProb ) {
            ans = true;
        }
        else {
            ans = false;
        }
        return ans;
    }

    static void analyzeResults() {
        System.out.println( "--------------------" );
        System.out.println( "Compensation: " + compensation + "€");
        System.out.println( "OC probability: " + acceptProb + "%" );
        analyzeCost();
        analyzePackages();
        System.out.println();
    }

    static void analyzeCost() {
        double sumCost = 0.0;

        for( int obs = 0; obs < OBSERVATIONS; obs++ ) {
            sumCost = sumCost + totalCost[obs][DAYS-1];
        }

        double avgCost = (double) sumCost / OBSERVATIONS;
        double variance = 0.0;

        for( int obs = 0; obs < OBSERVATIONS; obs++ ) {
            variance = variance + Math.pow( totalCost[obs][DAYS-1] - avgCost, 2.0 );
        }

        variance = (double) variance / ( OBSERVATIONS - 1 );

        double aux = (double) variance / OBSERVATIONS;

        double result = distFact * Math.sqrt( aux );

        double minCost = avgCost - result;
        double maxCost = avgCost + result;

        DecimalFormat df = new DecimalFormat("0.0");
        System.out.println( "\nTotal cost: (" + df.format(minCost) + " ; " + df.format(maxCost) + ")" );

        if( bestVal < 0.0 ) {
            bestVal = minCost;
            bestComp = compensation;
            bestProb = acceptProb;
            return;
        }

        if( minCost < bestVal ) {
            bestVal = minCost;
            bestComp = compensation;
            bestProb = acceptProb;
        }
    }

    static void analyzePackages() {
        double sumPackages = 0.0;

        for( int obs = 0; obs < OBSERVATIONS; obs++ ) {
            sumPackages = sumPackages + statusMax[obs];
        }

        double avgCost = (double) sumPackages / OBSERVATIONS;
        double variance = 0.0;

        for( int obs = 0; obs < OBSERVATIONS; obs++ ) {
            variance = variance + Math.pow( statusMax[obs] - avgCost, 2.0 );
        }

        variance = (double) variance / ( OBSERVATIONS - 1 );

        double aux = (double) variance / OBSERVATIONS;

        double result = distFact * Math.sqrt( aux );

        double minPackages = avgCost - result;
        double maxPackages = avgCost + result;

        DecimalFormat df = new DecimalFormat("0");
        System.out.println( "\nMaximum number of packages stored: (" + df.format(Math.ceil(minPackages)) + " ; " + df.format(Math.ceil(maxPackages)) + ")" );
    }

    static void printBestCompensation() {
        System.out.println( "--------------------" );
        System.out.println( "\nBest compensation: " + bestComp + "€");
        System.out.println( "OC probability: " + bestProb + "%");
        System.out.println();
    }

    public static void main( String[] args ) {
        if( args.length > 0 ) {
            if( args[0].equals( "samples" ) ) {
                mainSamples();
                return;
            }
        }

        int N = 5;

        double[] c = { 0.0, 0.5, 1.0, 1.5, 1.8 };
        int[] a = { 1, 25, 50, 60, 75 };

        bestVal = -1.0;
        bestComp = 0.0;
        bestProb = 0;

        calculateDistFact();

        for( int i = 0; i < N; i++ ) {
            compensation = c[i];
            acceptProb = a[i];
            simulation();
            analyzeResults();
        }
        printBestCompensation();
    }

    static void mainSamples() {
        int N = 5;

        double[] c = { 0.0, 0.5, 1.0, 1.5, 1.8 };
        int[] a = { 1, 25, 50, 60, 75 };

        bestVal = -1.0;
        bestComp = 0.0;
        bestProb = 0;

        calculateDistFact();

        for( int i = 0; i < N; i++ ) {
            compensation = c[i];
            acceptProb = a[i];
            simulation();
            printObservation( i , 0 );
            analyzeResults();
        }
        printBestCompensation();
    }

    static void printObservation( int id, int obs ) {
        System.out.print( "-------------------------------------------" );
        System.out.print( "-------------------------------------------" );
        System.out.println( "----------------------" );

        System.out.println( "Test id: " + (id + 1) );
        System.out.println( "Note: This are the results from a SINGLE observation using the following values" );
        System.out.println( "Compensation: " + compensation + "€");
        System.out.println( "OC probability: " + acceptProb + "%\n\n" );

        System.out.print( " DAY |" );
        System.out.print( " NEW PACKAGES |" );
        System.out.print( "     DELIVERIES     |" );
        System.out.print( " ACCUMULATED DELIVERIES |" );
        System.out.print( "         COSTS          |" );
        System.out.print( " LOCKER STATUS |" );
        System.out.println();

        System.out.print( " t   |" );
        System.out.print( " home    lckr |" );
        System.out.print( " pf     oc     lckr |" );
        System.out.print( " pf       oc       lckr |" );
        System.out.print( " pf       oc        ACC |" );
        System.out.print( " home     lckr |" );
        System.out.println();

        System.out.print( "-------------------------------------------" );
        System.out.print( "-------------------------------------------" );
        System.out.println( "----------------------" );

        for( int day = 0; day < DAYS; day++ ) {
            boolean printed = false;
            if( (day + 1) / 100 > 0) {
                System.out.print( " " + (day + 1) + " |" );
                printed = true;
            }
            if( !printed && (day + 1) / 10 > 0 ) {
                System.out.print( " " + (day + 1) + "  |" );
                printed = true;
            }
            if( !printed ) {
                System.out.print( " " + (day + 1) + "   |" );
                printed = true;
            }

            int newPackagesHome = npHome[obs][day];
            int newPackagesLocker = npLocker[obs][day];

            if( newPackagesHome / 10 > 0 ) {
                System.out.print( " " + newPackagesHome );
            }
            else {
                System.out.print( " " + newPackagesHome + " " );
            }

            if( newPackagesLocker / 10 > 0 ) {
                System.out.print( "      " + newPackagesLocker + "   |" );
            }
            else {
                System.out.print( "      " + newPackagesLocker + "    |" );
            }

            int deliveriesPf = delivPf[obs][day];
            printed = false;

            if( deliveriesPf / 100 > 0 ) {
                System.out.print( " " +  deliveriesPf + "   ");
                printed = true;
            }
            if( !printed && deliveriesPf / 10 > 0 ) {
                System.out.print( " " +  deliveriesPf + "    " );
                printed = true;
            }
            if( !printed ) {
                System.out.print( " " +  deliveriesPf + "     " );
                printed = true;
            }

            int deliveriesOc = delivOc[obs][day];
            printed = false;

            if( deliveriesOc / 100 > 0 ) {
                System.out.print( " " +  deliveriesOc + "   " );
                printed = true;
            }
            if( !printed && deliveriesOc / 10 > 0 ) {
                System.out.print( " " +  deliveriesOc + "    " );
                printed = true;
            }
            if( !printed ) {
                System.out.print( " " +  deliveriesOc + "     " );
                printed = true;
            }
            
            int deliveriesLocker = delivLocker[obs][day];
            printed = false;

            if( deliveriesLocker / 100 > 0 ) {
                System.out.print( " " +  deliveriesLocker + "  |" );
                printed = true;
            }
            if( !printed && deliveriesLocker / 10 > 0 ) {
                System.out.print( " " +  deliveriesLocker + "   |" );
                printed = true;
            }
            if( !printed ) {
                System.out.print( " " +  deliveriesLocker + "    |" );
                printed = true;
            }

            int accDeliveriesPf = accDelivPf[obs][day];
            printed = false;

            if( accDeliveriesPf / 1000 > 0 ) {
                System.out.print( " " +  accDeliveriesPf + "    ");
                printed = true;
            }
            if( !printed && accDeliveriesPf / 100 > 0 ) {
                System.out.print( " " +  accDeliveriesPf + "     " );
                printed = true;
            }
            if( !printed && accDeliveriesPf / 10 > 0 ) {
                System.out.print( " " +  accDeliveriesPf + "      " );
                printed = true;
            }
            if( !printed ) {
                System.out.print( " " +  accDeliveriesPf + "       " );
                printed = true;
            }

            int accDeliveriesOc = accDelivOc[obs][day];
            printed = false;

            if( accDeliveriesOc / 1000 > 0 ) {
                System.out.print( " " +  accDeliveriesOc + "    ");
                printed = true;
            }
            if( !printed && accDeliveriesOc / 100 > 0 ) {
                System.out.print( " " +  accDeliveriesOc + "     " );
                printed = true;
            }
            if( !printed && accDeliveriesOc / 10 > 0 ) {
                System.out.print( " " +  accDeliveriesOc + "      " );
                printed = true;
            }
            if( !printed ) {
                System.out.print( " " +  accDeliveriesOc + "       " );
                printed = true;
            }

            int accDeliveriesLocker = accDelivLocker[obs][day];
            printed = false;

            if( accDeliveriesLocker / 1000 > 0 ) {
                System.out.print( " " +  accDeliveriesLocker + " |");
                printed = true;
            }
            if( !printed && accDeliveriesLocker / 100 > 0 ) {
                System.out.print( " " +  accDeliveriesLocker + "  |" );
                printed = true;
            }
            if( !printed && accDeliveriesLocker / 10 > 0 ) {
                System.out.print( " " +  accDeliveriesLocker + "   |" );
                printed = true;
            }
            if( !printed ) {
                System.out.print( " " +  accDeliveriesLocker + "    |" );
                printed = true;
            }

            DecimalFormat df = new DecimalFormat("0.0");

            double printCostPf = costPf[obs][day];
            int auxCostPf = (int) printCostPf;
            printed = false;

            if( auxCostPf / 1000 > 0 ) {
                System.out.print( " " +  df.format(printCostPf) + " ");
                printed = true;
            }
            if( !printed && auxCostPf / 100 > 0 ) {
                System.out.print( " " +  df.format(printCostPf) + "  " );
                printed = true;
            }
            if( !printed && auxCostPf / 10 > 0 ) {
                System.out.print( " " +  df.format(printCostPf) + "   " );
                printed = true;
            }
            if( !printed ) {
                System.out.print( " " +  df.format(printCostPf) + "    " );
                printed = true;
            }

            double printCostOc= costOc[obs][day];
            int auxCostOc = (int) printCostOc;
            printed = false;

            if( auxCostOc / 1000 > 0 ) {
                System.out.print( " " +  df.format(printCostOc) + " " );
                printed = true;
            }
            if( !printed && auxCostOc / 100 > 0 ) {
                System.out.print( " " +  df.format(printCostOc) + "  " );
                printed = true;
            }
            if( !printed && auxCostOc / 10 > 0 ) {
                System.out.print( " " +  df.format(printCostOc) + "   " );
                printed = true;
            }
            if( !printed ) {
                System.out.print( " " +  df.format(printCostOc) + "    " );
                printed = true;
            }

            double printTotalCost= totalCost[obs][day];
            int auxTotalCost = (int) printTotalCost;
            printed = false;

            if( auxTotalCost / 1000 > 0 ) {
                System.out.print( " " +  df.format(printTotalCost) + " |" );
                printed = true;
            }
            if( !printed && auxTotalCost / 100 > 0 ) {
                System.out.print( "  " +  df.format(printTotalCost) + " |" );
                printed = true;
            }
            if( !printed && auxTotalCost / 10 > 0 ) {
                System.out.print( "   " +  df.format(printTotalCost) + " |" );
                printed = true;
            }
            if( !printed ) {
                System.out.print( "    " +  df.format(printTotalCost) + " |" );
                printed = true;
            }

            int printStatusHome = statusHome[obs][day];
            printed = false;

            if( printStatusHome / 1000 > 0 ) {
                System.out.print( " " +  printStatusHome + "    " );
                printed = true;
            }
            if( !printed && printStatusHome / 100 > 0 ) {
                System.out.print( " " +  printStatusHome + "     " );
                printed = true;
            }
            if( !printed && printStatusHome / 10 > 0 ) {
                System.out.print( " " +  printStatusHome + "      " );
                printed = true;
            }
            if( !printed ) {
                System.out.print( " " +  printStatusHome + "       " );
                printed = true;
            }

            int printStatusLocker = statusLocker[obs][day];
            printed = false;

            if( printStatusLocker / 1000 > 0 ) {
                System.out.print( " " +  printStatusLocker + " |" );
                printed = true;
            }
            if( !printed && printStatusLocker / 100 > 0 ) {
                System.out.print( " " +  printStatusLocker + "  |" );
                printed = true;
            }
            if( !printed && printStatusLocker / 10 > 0 ) {
                System.out.print( " " +  printStatusLocker + "   |" );
                printed = true;
            }
            if( !printed ) {
                System.out.print( " " +  printStatusLocker + "    |" );
                printed = true;
            }

            System.out.println();
        }
    }
} 