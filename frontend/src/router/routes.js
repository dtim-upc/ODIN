
const routes = [
  {
    path: '/',
    // component: () => import('layouts/MainLayout.vue'),
    redirect: to => {
      // the function receives the target route as the argument
      // we return a redirect path/location here.
      return { path: '/projects', name:'projects' }
    },
  },
  {
    path: '/project/:id',
    // name: 'dashboard',
    component: () => import('layouts/MainLayout.vue'),
    children: [
      { path: 'home', name:'home', component: () => import('pages/datasources/HomeDashboard.vue') },
      { path: 'schema', name:'schema',  component: () => import('pages/datasources/Schema.vue') },

      { path: 'datasources', name:'datasources',  component: () => import('pages/datasources/DataSources.vue') },
      { path: 'viewTriples/:datasourceID', name:'viewTriples',  component: () => import('pages/datasources/ViewTriples.vue') },

      { path: 'configureIntegration', name:'dsIntegration',  component: () => import('pages/datasources/DataSourceIntegration.vue') },

      { path: 'query', name:'query',  component: () => import('pages/datasources/Query.vue') },

    ]
  },
  {
    path: '/projects',
    component: () => import('layouts/ProjectLayout.vue'),
    children: [
        {path: '' , name: 'projects', component: () => import('pages/Projects.vue')}
    ]
  },
  {
    path: '/viewGraph',
    component: () => import('layouts/ProjectLayout.vue'),
    children: [
        {path: '' , name: 'vg', component: () => import('pages/datasources/ViewGraph.vue') }
    ]

  },
  //{
  //  path: '/auth',
    //name: 'auth',
    //component: () => import('pages/Auth.vue'),
  //},

  // Always leave this as last one,
  // but you can also remove it
  {
    path: '/:catchAll(.*)*',
    component: () => import('pages/ErrorNotFound.vue')
  }
]

export default routes
