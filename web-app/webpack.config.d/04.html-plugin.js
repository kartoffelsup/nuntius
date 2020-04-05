;(function (config) {
    const HtmlWebpackPlugin = require('html-webpack-plugin');

    config.node = {fs: 'empty'}
    config.plugins.push(new HtmlWebpackPlugin({
        template: '../../../../web-app/src/main/resources/index.ejs'
    }))
})(config);
