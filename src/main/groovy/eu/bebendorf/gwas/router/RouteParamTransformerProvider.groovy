package eu.bebendorf.gwas.router

interface RouteParamTransformerProvider {
    List<RouteParamTransformer> getRouteParamTransformers()
    RouteParamTransformer getRouteParamTransformer(String type)
}