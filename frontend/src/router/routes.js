const routes = [
  {
    path: '/',
    redirect: to => {
      // the function receives the target route as the argument and we return a redirect path/location here.
      return {path: '/projects', name: 'projects'}
    },
  },
  {
    path: '/project/:id',
    component: () => import('layouts/MainLayout.vue'),
    children: [
      // Pages that appear in the main layout as it is
      {path: 'home', name: 'home', component: () => import('pages/HomeDashboard.vue')},
      {path: 'repositories', name: 'repositories', component: () => import('pages/Repositories.vue')},
      {path: 'datasets', name: 'datasets', component: () => import('pages/Datasets.vue')},
      {path: 'schema', name: 'schema', component: () => import('pages/Schema.vue')},
      {path: 'query', name: 'query', component: () => import('pages/Query.vue')},
      {path: 'data-products', name: 'data-products', component: () => import('pages/DataProducts.vue')},
      {path: 'intents', name: 'intents', component: () => import('pages/Intents.vue')},
      // Process to integrate a dataset
      {path: 'configureIntegration', name: 'dsIntegration', component: () => import('pages/DatasetIntegration.vue')},
      // Steps to create an intent
      {path: 'abstract-planner', name: 'abstract-planner', component: () => import('pages/CreateIntent.vue')},
      {path: 'logical-planner', name: 'logical-planner', component: () => import('pages/CreateIntent.vue')},
      {path: 'workflow-planner', name: 'workflow-planner', component: () => import('pages/CreateIntent.vue')},
      {path: 'intent-workflows', name: 'intent-workflows', component: () => import('pages/CreateIntent.vue')},
    ]
  },

  // Page that appears when no project has been selected yet
  {
    path: '/projects',
    component: () => import('layouts/ProjectLayout.vue'),
    children: [
      {path: '', name: 'projects', component: () => import('pages/Projects.vue')}
    ]
  },

  // When the specified route is not found, redirect to the 404 page
  {
    path: '/:catchAll(.*)*',
    component: () => import('pages/ErrorNotFound.vue')
  }
]

export default routes
