# How do I output CSV file?

You can use commons-csv & CallbackResponse for generating CSV files.

Add dependency for pom.xml.

    <dependency>
      <groupId>org.apache.commons</groupId>
      <artifactId>commons-csv</artifactId>
      <version>1.1</version>
    </dependency>

Implement the code on your controller.

    @GET("/csv")
    public WebResponse csv() {
        return new CallbackResponse(resp -> {
            resp.setStatus(200);
            resp.setContentType("text/csv; charset=UTF-8");
            resp.setHeader("Content-Disposition", "attachment;filename=my-file-name.csv");

            CSVFormat format = CSVFormat.EXCEL;
            try (Writer writer = resp.getWriter()) {
                try (final CSVPrinter csvPrinter = new CSVPrinter(writer, format)) {
                    csvPrinter.printRecord(Arrays.asList("こんにちは", "世界"));
                    // You can pass the ResultSet object directly.
                }
            }
        });
    }

(This code may works well... But I don't tested with Excel on Windows... Please send me p-r, if there is an issue.)

