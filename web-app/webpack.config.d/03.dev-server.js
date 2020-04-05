;(function (config) {
   const devServer = config.devServer
   if(devServer) {
       config.devServer = Object.assign(devServer, {
           port: 9000,
           historyApiFallback: true,
           proxy: {
                 '/api': 'http://localhost:8080'
           }
       })
   }
})(config);
