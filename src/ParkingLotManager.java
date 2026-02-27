import java.util.*;

public class ParkingLotManager {

    private static final int TOTAL_SPOTS = 500;
    private static final double HOURLY_RATE = 5.0;

    enum Status { EMPTY, OCCUPIED, DELETED }

    static class ParkingSpot {
        String licensePlate;
        long entryTime;
        Status status;

        ParkingSpot() {
            status = Status.EMPTY;
        }
    }

    private ParkingSpot[] table;
    private int occupiedSpots = 0;
    private long totalProbes = 0;
    private long totalParks = 0;

    public ParkingLotManager() {
        table = new ParkingSpot[TOTAL_SPOTS];
        for (int i = 0; i < TOTAL_SPOTS; i++) {
            table[i] = new ParkingSpot();
        }
    }

    // Custom hash function
    private int hash(String licensePlate) {
        return Math.abs(licensePlate.hashCode()) % TOTAL_SPOTS;
    }

    // Park vehicle using linear probing
    public void parkVehicle(String licensePlate) {
        int index = hash(licensePlate);
        int probes = 0;

        for (int i = 0; i < TOTAL_SPOTS; i++) {
            int currentIndex = (index + i) % TOTAL_SPOTS;

            if (table[currentIndex].status == Status.EMPTY ||
                    table[currentIndex].status == Status.DELETED) {

                table[currentIndex].licensePlate = licensePlate;
                table[currentIndex].entryTime = System.currentTimeMillis();
                table[currentIndex].status = Status.OCCUPIED;

                occupiedSpots++;
                totalProbes += probes;
                totalParks++;

                System.out.println("Assigned spot #" + currentIndex +
                        " (" + probes + " probes)");
                return;
            }
            probes++;
        }

        System.out.println("Parking Full!");
    }

    // Exit vehicle
    public void exitVehicle(String licensePlate) {
        int index = hash(licensePlate);

        for (int i = 0; i < TOTAL_SPOTS; i++) {
            int currentIndex = (index + i) % TOTAL_SPOTS;

            if (table[currentIndex].status == Status.EMPTY) {
                break;
            }

            if (table[currentIndex].status == Status.OCCUPIED &&
                    table[currentIndex].licensePlate.equals(licensePlate)) {

                long exitTime = System.currentTimeMillis();
                long durationMs = exitTime - table[currentIndex].entryTime;

                double hours = durationMs / (1000.0 * 60 * 60);
                double fee = Math.ceil(hours) * HOURLY_RATE;

                table[currentIndex].status = Status.DELETED;
                occupiedSpots--;

                System.out.println("Spot #" + currentIndex +
                        " freed. Duration: " +
                        String.format("%.2f", hours) +
                        " hrs, Fee: $" + fee);
                return;
            }
        }

        System.out.println("Vehicle not found.");
    }

    // Find nearest available spot (from entrance = index 0)
    public int findNearestAvailable() {
        for (int i = 0; i < TOTAL_SPOTS; i++) {
            if (table[i].status == Status.EMPTY ||
                    table[i].status == Status.DELETED) {
                return i;
            }
        }
        return -1;
    }

    // Generate statistics
    public void getStatistics() {
        double occupancyRate =
                (occupiedSpots * 100.0) / TOTAL_SPOTS;

        double avgProbes =
                totalParks == 0 ? 0 :
                        (double) totalProbes / totalParks;

        System.out.println("Occupancy: " +
                String.format("%.2f", occupancyRate) + "%");
        System.out.println("Avg Probes: " +
                String.format("%.2f", avgProbes));
        System.out.println("Load Factor: " +
                String.format("%.2f",
                        (double) occupiedSpots / TOTAL_SPOTS));
    }

    // Main method
    public static void main(String[] args)
            throws InterruptedException {

        ParkingLotManager manager =
                new ParkingLotManager();

        manager.parkVehicle("ABC-1234");
        manager.parkVehicle("ABC-1235");
        manager.parkVehicle("XYZ-9999");

        Thread.sleep(3000); // simulate time

        manager.exitVehicle("ABC-1234");

        manager.getStatistics();

        System.out.println("Nearest Available Spot: #" +
                manager.findNearestAvailable());
    }
}