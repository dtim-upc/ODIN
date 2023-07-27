<template>

    <div class="folder" :class="activeFolder == props.row.projectId ? 'active' : ''" >
        <div class="folder__back" :style="folderBackColor">
            <div class="paper"></div>
            <div class="paper"></div>
            <div class="paper"></div>
            <div class="folder__front" :style="folderFrontColor"></div>
            <div class="folder__front right" :style="folderFrontColor" @click="openFolder(props.row)" >
                <!-- q-mt-md -->
                <!-- <div class="col"> -->
                <div class="row no-wrap items-center  q-pa-sm rounded-borders">
                    <span> {{ props.row.projectName }}</span>
                    <q-space />
                    <q-btn-dropdown @show="activeFolder = props.row.projectId" @before-hide="activeFolder = ''" color="primary"
                        flat dropdown-icon="more_horiz" no-icon-animation padding="none" menu-anchor="top right"
                        menu-self="top left" @click.stop.prevent>
                        <q-list dense>
                            <q-item clickable v-close-popup @click="onItemClick">
                                <q-item-section avatar style="min-width: 30px;padding:0">
                                    <q-icon color="primary" name="settings" />
                                </q-item-section>
                                <q-item-section>
                                    <q-item-label>Settings</q-item-label>
                                </q-item-section>
                            </q-item>

                            <q-item clickable v-close-popup @click="onItemClick">
                                <q-item-section avatar style="min-width: 30px; padding:0">
                                    <q-icon color="primary" name="folder_copy" />
                                </q-item-section>
                                <q-item-section>
                                    <q-item-label>Clone</q-item-label>
                                </q-item-section>
                            </q-item>

                            <q-item clickable v-close-popup @click="onItemClick">
                                <q-item-section avatar style="min-width: 30px;padding:0">
                                    <q-icon color="primary" name="join_full" />
                                </q-item-section>
                                <q-item-section>
                                    <q-item-label>Integrate</q-item-label>
                                </q-item-section>
                            </q-item>

                            <q-item clickable v-close-popup @click="deleteItem(props.row.projectId)">
                                <q-item-section avatar style="min-width: 30px;padding:0">
                                    <q-icon color="primary" name="delete" />
                                </q-item-section>
                                <q-item-section>
                                    <q-item-label>Delete</q-item-label>
                                </q-item-section>
                            </q-item>
                        </q-list>
                    </q-btn-dropdown>
                </div>
                <div style="position:absolute;bottom:0;width:100%">
                    <div class="row no-wrap items-center q-mt-md q-pa-sm rounded-borders">
                        <!-- <span> {{ props.row.createdBy }}</span> -->
                        <q-chip :style="folderBackColor" text-color="white" >
                            {{props.row.datasets.length}} files
                        </q-chip>
                        <q-space />
                        <span>{{ props.row.privacy }}</span>
                    </div>
                </div>
            </div>
            <div class="folder__back_after" :style="folderBackColor">
            </div>

        </div>

    </div>


</template>



<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { colors } from 'quasar'
import { useDataSourceStore } from 'src/stores/datasources.store.js'
import dataSourcesAPI from "src/api/dataSourcesAPI";
import projectAPI from "src/api/projectAPI";
import {useProjectsStore} from "stores/projects.store";

const props = defineProps({
    row: {type:Object},
    folderColor: {type:String, default: "#3dbb94"}
});
const projectsStore = useProjectsStore()
const router = useRouter()

const activeFolder = ref("")

const folderBackColor = computed(() => {
    return 'background:' +colors.lighten(props.folderColor, -10)+';'
})

const folderFrontColor = computed(() => 'background:' +props.folderColor+';')

const openFolder = (project) => {
    router.push({name: 'home', params: {id: project.projectId}})
}
const onItemClick = (project, event) => {
  const option = event.currentTarget.innerText;
  switch (option) {
    case 'Delete':
      deleteItem(project.projectId);
      break;
    // Handle other options if needed
    default:
      break;
  }
};

const token = 'your_token_value_here';

const deleteItem = (id) => {
  // Perform deletion logic here
  projectsStore.deleteProjectByID(id, token)
    .then(() => {
      console.log('Item deleted');
    })
    .catch((error) => {
      console.error('Error deleting item:', error);
    });
};



</script>

<style lang="scss">
$folderColor: #70a1ff;
$paperColor: #ffffff;


.folder {
    transition: all 0.2s ease-in;
    margin: 10px;
    width: 100%;

    &__back {
        position: relative;
        width: 100%;
        padding: 35%;
        border-radius: 0px 5px 5px 5px;

        .folder__back_after{
            position: absolute;
            bottom: 98%; //if 100% you can see a little gap on Chrome
            left: 0;
            content: "";
            width: 35%;
            height: 10%;
            border-radius: 5px 5px 0 0;
        }

        .paper {
            position: absolute;
            bottom: 10%;
            left: 50%;
            transform: translate(-50%, 10%);
            width: 70%;
            height: 80%;
            background: darken($paperColor, 10%);
            border-radius: 5px;
            transition: all 0.3s ease-in-out;

            //make paper bigger and bigger

            &:nth-child(2) {
                background: darken($paperColor, 5%);
                width: 80%;
                height: 70%;
            }

            &:nth-child(3) {
                background: darken($paperColor, 0%);
                width: 90%;
                height: 60%;
            }
        }

        .folder__front {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            // background: $folderColor;
            border-radius: 5px;
            transform-origin: bottom;
            transition: all 0.3s ease-in-out;
        }
    }

    &.active {
        transform: translateY(-8px);
    }

    &.active .paper {
        transform: translate(-50%, 0%);
    }

    //there are 2 parts for the front of folder
    //one goes left and another goes right

    //   &:hover .folder__front {
    //     transform: skew(15deg) scaleY(0.6);
    //   }

    //   &:hover .right {
    //     transform: skew(-15deg) scaleY(0.6);
    //   }

    &.active .folder__front {
        transform: skew(15deg) scaleY(0.6);
    }

    &.active .right {
        transform: skew(-15deg) scaleY(0.6);
    }
}
</style>
