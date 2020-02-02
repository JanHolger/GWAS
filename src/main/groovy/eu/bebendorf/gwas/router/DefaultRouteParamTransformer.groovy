package eu.bebendorf.gwas.router

class DefaultRouteParamTransformer extends RouteParamTransformer {
    static final RouteParamTransformer INSTANCE = new DefaultRouteParamTransformer()
    DefaultRouteParamTransformer(){
        add("s|string", "[0-9A-Za-z %:\\-\\+\\.]+", { String s -> s })
        add("short", "\\-?[0-9]+", { String s -> Short.parseShort(s) })
        add("i|int|integer", "\\-?[0-9]+", { String s -> Integer.parseInt(s) })
        add("i*|int*|integer*", "[0-9]+", { String s -> Integer.parseInt(s) })
        add("i+|int+|integer+", "[1-9][0-9]*", { String s -> Integer.parseInt(s) })
        add("i-|int-|integer-", "\\-[1-9][0-9]*", { String s -> Integer.parseInt(s) })
        add("l|long", "\\-?[0-9]+", { String s -> Long.parseLong(s) })
        add("l*|long*", "[0-9]+", { String s -> Long.parseLong(s) })
        add("l+|long+", "[1-9][0-9]*", { String s -> Long.parseLong(s) })
        add("l-|long-", "\\-[1-9][0-9]*", { String s -> Long.parseLong(s) })
        add("f|float", "\\-?[0-9]+(\\.[0-9]*)?", { String s -> Float.parseFloat(s) })
        add("d|double", "\\-?[0-9]+(\\.[0-9]*)?", { String s -> Double.parseDouble(s) })
        add("b|bool|boolean", "([Tt]rue|[Ff]alse|0|1)", { String s -> s.equalsIgnoreCase("true") || s == "1" })
    }
}
