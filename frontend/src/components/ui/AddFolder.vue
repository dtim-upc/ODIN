<template>

    <div class="folder" @click="$emit('addFolder')">
        <div class="folder__back" :style="folderBackColor">
            <div class="paper"></div>
            <div class="paper"></div>
            <div class="paper"></div>
            <div class="folder__front" :style="folderFrontColor"></div>
            <div class="folder__front right" :style="folderFrontColor" >
                <!-- q-mt-md -->
                <!-- <div class="col"> -->
                <div class="text-center flex flex-center" style="height:100%">
                     <q-icon size="xl" name="add" />
                </div>


            </div>
            <div class="folder__back_after" :style="folderBackColor">
            </div>

        </div>

    </div>


</template>



<script setup>
import { ref, computed } from "vue";
import { colors } from 'quasar'
import { odinApi } from "boot/axios";
import TableQueryResult from "components/tables/TableQueryResult.vue"

const props = defineProps({
    row: {type:Object},
    folderColor: {type:String, default: "#fff"} //#F2E9E9 //#3dbb94
});
const emit = defineEmits(["addFolder"])
const activeFolder = ref("")

const folderBackColor = computed(() => {
    // return 'background:' +colors.lighten(props.folderColor, -10)+';'
    return 'background:' +colors.lighten(props.folderColor,0)+';'
})

const folderFrontColor = computed(() => 'background:' +props.folderColor+';')

</script>

<style lang="scss">
$folderColor: $neutral0 #70a1ff;
$paperColor: #ffffff;


.folder {
    transition: all 0.2s ease-in;
    margin: 10px;
    width: 100%;

    &__back {
        position: relative;
        // width: 100%;
        width: 100%;
        padding: 35%;
        // width: 100px;
        // min-height: 80px;
        // background: darken($folderColor, 8%);
        border-radius: 0px 5px 5px 5px;


        //The thing on the left top
        // &::after {
        .folder__back_after{
            position: absolute;
            bottom: 98%; //if 100% you can see a little gap on Chrome
            left: 0;
            content: "";
            width: 35%;
            height: 10%;
            //   width: 30px;
            //   height: 10px;
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
