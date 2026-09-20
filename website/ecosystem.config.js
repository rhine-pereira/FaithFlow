const path = require('path');

module.exports = {
  apps: [
    {
      name: 'faithflow-web',
      script: path.join(__dirname, 'node_modules', 'next', 'dist', 'bin', 'next'),
      args: 'start -p 3008',
      cwd: __dirname,
      instances: 1, // Set to 'max' for multi-threaded cluster mode across all CPU cores
      exec_mode: 'fork', // Set to 'cluster' if instances > 1
      autorestart: true,
      watch: false,
      max_memory_restart: '1G',
      env: {
        NODE_ENV: 'production',
        PORT: 3008,
      },
      env_production: {
        NODE_ENV: 'production',
        PORT: 3008,
      },
    },
  ],
};
