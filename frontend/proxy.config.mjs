const backendHost = process.env.BACKEND_HOST ?? '127.0.0.1';
const backendPort = process.env.BACKEND_PORT ?? '8080';

/**
 * @type {import('vite').CommonServerOptions['proxy']}
 */
export default {
  '^/(api|management|v3/api-docs|services)': {
    target: `http://${backendHost}:${backendPort}`,
    xfwd: true,
  },
};
