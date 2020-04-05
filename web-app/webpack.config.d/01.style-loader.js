;(function (config) {
    let loaders = ['style-loader', 'css-loader'];
    if (!config.devServer) {
        const MiniCssExtractPlugin = require('mini-css-extract-plugin');
        loaders = [MiniCssExtractPlugin.loader, 'css-loader']
         config.plugins.push(new MiniCssExtractPlugin())
    }

    config.module.rules.push({
        test: /\.css$/,
         use: loaders
    });
})(config);
