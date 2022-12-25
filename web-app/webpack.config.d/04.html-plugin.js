;(function (config) {
    const HtmlWebpackPlugin = require('html-webpack-plugin');

    config.plugins.push(new HtmlWebpackPlugin({
        template: '../../../../web-app/src/jsMain/resources/index.ejs'
    }))
})(config);
