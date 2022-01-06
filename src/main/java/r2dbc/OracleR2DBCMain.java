package r2dbc;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactoryOptions;
import oracle.r2dbc.OracleR2dbcOptions;
import reactor.core.publisher.Mono;


public class OracleR2DBCMain {
    private static String HOST ="10.40.62.166";
    private static String PORT ="10101";
    private static String USER="tmalluser";
    private static String PASSWORD="tmalluser#stg1";
    private static String SERVICE_NAME="TMALL";

    private static final String DESCRIPTOR = "(DESCRIPTION=" +
            "(ADDRESS=(HOST="+HOST+")(PORT="+PORT+")(PROTOCOL=tcp))" +
            "(CONNECT_DATA=(SERVICE_NAME="+SERVICE_NAME+")))";

    public static void main(String[] args) {
        // A descriptor may appear in the query section of an R2DBC URL:
        String r2dbcUrl = "r2dbc:oracle://?oracle.r2dbc.descriptor="+DESCRIPTOR;
        Mono.from(ConnectionFactories.get(ConnectionFactoryOptions.parse(r2dbcUrl)
                                .mutate()
                                .option(ConnectionFactoryOptions.USER, USER)
                                .option(ConnectionFactoryOptions.PASSWORD, PASSWORD)
                                .build())
                        .create())
                .flatMapMany(connection ->
                        Mono.from(connection.createStatement(
                                                "SELECT sysdate FROM sys.dual")
                                        .execute())
                                .flatMapMany(result ->
                                        result.map(row -> row.get(0, String.class)))
                                .concatWith(Mono.from(connection.close()).cast(String.class)))
                .toStream()
                .forEach(System.out::println);

        // A descriptor may also be specified as an Option
        Mono.from(ConnectionFactories.get(ConnectionFactoryOptions.builder()
                                .option(ConnectionFactoryOptions.DRIVER, "oracle")
                                .option(OracleR2dbcOptions.DESCRIPTOR, DESCRIPTOR)
                                .option(ConnectionFactoryOptions.USER, USER)
                                .option(ConnectionFactoryOptions.PASSWORD, PASSWORD)
                                .build())
                        .create())
                .flatMapMany(connection ->
                        Mono.from(connection.createStatement(
                                                "SELECT 'Connected with TNS descriptor' FROM sys.dual")
                                        .execute())
                                .flatMapMany(result ->
                                        result.map((row, metadata) -> row.get(0, String.class)))
                                .concatWith(Mono.from(connection.close()).cast(String.class)))
                .toStream()
                .forEach(System.out::println);
    }
}
