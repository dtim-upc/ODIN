<template>
  <div style="height: 70vh">
    <VueFlow
      v-model:nodes="nodes"
      v-model:edges="edges"
      fit-view-on-init
      class="vue-flow-basic-example"
      :default-zoom="1.5"
      :min-zoom="0.2"
      :max-zoom="4"
    >
      <Background pattern-color="#aaa" :gap="8" />

      <MiniMap />

      <Controls />

      <template #node-custom="nodeProps">
        <CustomNode v-bind="nodeProps" />
      </template>

      <template #edge-custom="edgeProps">
        <CustomEdge v-bind="edgeProps" />
      </template>
    </VueFlow>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import { MiniMap } from '@vue-flow/minimap'
import { VueFlow, useVueFlow, type Node, type Edge, Position} from '@vue-flow/core'
import CustomNode from './CustomNode.vue'
import CustomEdge from './CustomEdge.vue'
import ELK from 'elkjs/lib/elk.bundled.js'

const props = defineProps(['plan'])
const { onConnect, addEdges } = useVueFlow()

/* Reference structure of the nodes and edges
const nodes = ref<Node[]>([
  { id: 'https://extremexp.eu/ontology/cbox#DataLoading', type: 'input', label: 'DataLoading', position: { x: 0, y: 0 }, sourcePosition: Position.Right, class: 'grey-node'},
  { id: 'https://extremexp.eu/ontology/cbox#DataStoring', type: 'output', label: 'DataStoring', position: { x: 800, y: 0 }, targetPosition: Position.Left, class: 'grey-node'},
  { id: 'https://extremexp.eu/ontology/cbox#DecisionTree', label: 'DecisionTree', position: { x: 600, y: 0 }, sourcePosition: Position.Right, targetPosition: Position.Left, class: 'grey-node'},
  { id: 'https://extremexp.eu/ontology/cbox#DecisionTree-Train', label: 'DecisionTree Train', position: { x: 400, y: 60 }, sourcePosition: Position.Right, targetPosition: Position.Left, class: 'grey-node' },
  { id: 'https://extremexp.eu/ontology/cbox#Partitioning', label: 'Partitioning', position: { x: 200, y: 0 }, sourcePosition: Position.Right, targetPosition: Position.Left, class: 'grey-node' },
])

const edges = ref<Edge[]>([
  { id: 'e0', source: 'https://extremexp.eu/ontology/cbox#DataLoading', target: 'https://extremexp.eu/ontology/cbox#Partitioning', type: 'input', animated: true },
  { id: 'e1', source: 'https://extremexp.eu/ontology/cbox#Partitioning', target: 'https://extremexp.eu/ontology/cbox#DecisionTree-Train', type: 'input', animated: true },
  { id: 'e2', source: 'https://extremexp.eu/ontology/cbox#Partitioning', target: 'https://extremexp.eu/ontology/cbox#DecisionTree', type: 'input', animated: true },
  { id: 'e3', source: 'https://extremexp.eu/ontology/cbox#DecisionTree-Train', target: 'https://extremexp.eu/ontology/cbox#DecisionTree', type: 'input', animated: true },
  { id: 'e4', source: 'https://extremexp.eu/ontology/cbox#DecisionTree', target: 'https://extremexp.eu/ontology/cbox#DataStoring', type: 'input', animated: true },
])*/

const nodes = ref<Node[]>([])
const edges = ref<Edge[]>([])

const elk = new ELK({
  defaultLayoutOptions: {
    'elk.algorithm': 'layered',
    'org.eclipse.elk.spacing.nodeNode': '20',
    'org.eclipse.elk.layered.spacing.edgeNodeBetweenLayers': '20',
    'org.eclipse.elk.spacing.edgeNode': '20',
  }
})

function toTitleCase(str: string): string {
  return str.replace(
    /\w\S*/g,
    function (txt) {
      return txt.charAt(0).toUpperCase() + txt.substring(1);
    }
  );
}

async function plan_layout(plan: { [key: string]: string[] }) {
  let nodes_plan = Object.keys(plan).map(node => {
    let node_id = node.split('#').at(-1)!;
    let label = toTitleCase(node_id.replaceAll('_', ' ').replaceAll('-', ' ').replace('component ', ''));
    let width = 200;
    let label_width = label.length * 12;
    let height = label_width > width ? 30 * label_width / width : 30;
    return {
      id: node,
      node_id: node_id,
      data: {
          label: label,
      },
      width: width,
      height: height,
      sourcePosition: 'right',
      targetPosition: 'left',
    }
  });

  let edges_plan = [];
  let i = 0;
  for (let source of Object.keys(plan)) {
    for (let target of plan[source]) {
      edges_plan.push({
        id: 'e' + i,
        sources: [source],
        targets: [target],
        source: source,
        target: target,
        arrow: true,
      });
      i++;
    }
  }

  const graph = {
    id: "root",
    children: nodes_plan,
    edges: edges_plan
  }

  const layout = await elk.layout(graph);

  layout.children!.map(node => {
    let node_id = node.id.split('#').at(-1)!;
    let label = toTitleCase(node_id.replaceAll('_', ' ').replaceAll('-', ' ').replace('component ', ''));
    nodes.value.push({
      id: node.id,
      label: label,
      position:{
        x: node.x as number,
        y: node.y as number,
      },
      sourcePosition: Position.Right,
      targetPosition: Position.Left,
      class: 'grey-node'
    })
  });

  layout.edges!.map(edge => {
    edges.value.push({
      id: edge.id,
      source: edge.sources.at(0) as string,
      target: edge.targets.at(0) as string,
    })
  })
}

plan_layout(props.plan)

onConnect((params) => {
  addEdges([params])
})
</script>

<style>
.grey-node {
  background: rgb(216, 206, 206);
}
</style>
