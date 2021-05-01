;(function (config) {
    config.module.rules.push({
        test: /.(png|woff(2)?|eot|ttf|svg)(\?[a-z0-9=\.]+)?$/,
        use: 'url-loader?limit=100000'
    });
})(config);
