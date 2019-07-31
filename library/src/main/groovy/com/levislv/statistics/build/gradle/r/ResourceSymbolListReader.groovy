package com.levislv.statistics.build.gradle.r

/**
 * @author levislv
 */
class ResourceSymbolListReader {
    private FinalRClassBuilder builder

    ResourceSymbolListReader(FinalRClassBuilder builder) {
        this.builder = builder
    }

    void readSymbolTable(File symbolTable) {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(symbolTable), 'UTF-8'))
        String line
        while ((line = br.readLine()) != null) {
            processLine(line)
        }
        br.close()
    }

    private void processLine(String line) {
        List<String> values = line.split(' ')
        if (values.size() < 4) {
            return
        }
        String javaType = values[0]
        if (!'int'.equals(javaType)) {
            return
        }
        String symbolType = values[1]
        if (!FinalRClassBuilder.SUPPORTED_TYPES.contains(symbolType)) {
            return
        }
        String name = values[2]
        String value = values[3]
        builder.addResourceField(symbolType, name, value)
    }
}
