import { Routes } from '@angular/router';
import { MainLayoutComponent } from './layout';

export const routes: Routes = [
  {
    path: '',
    component: MainLayoutComponent,
    children: [
      { path: '', redirectTo: 'rag', pathMatch: 'full' },
      {
        path: 'rag',
        loadComponent: () => import('./rag/rag.page').then(m => m.RagPageComponent),
      },
      {
        path: 'vision',
        loadComponent: () => import('./vision/vision.page').then(
          m => m.VisionPageComponent,
        ),
      },
      {
        path: 'ai-hubs',
        loadComponent: () => import('./ai-hub/ai-hub.page').then(m => m.AiHubPage),
      },
    ],
  },
];
